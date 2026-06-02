package com.dingky.gis.demo.title;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 本地并行处理器（策略一）
 * 
 * 特点：
 * - 单机多核并行处理
 * - 线程安全的原子性分组
 * - 适合千万级以下数据
 */
public class LocalParallelProcessor implements ShapefileProcessor {
    
    @Override
    public String getProcessorName() {
        return "Local Parallel Processor (v2.0)";
    }
    
    @Override
    public Map<String, List<String>> process(List<String> shpPaths) {
        System.out.println("使用处理器: " + getProcessorName());
        System.out.println("警告: GDAL 不是线程安全的，文件级处理将采用串行模式");
        
        // 全局共享的分组 Map（线程安全）
        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> globalGroupMap = new ConcurrentHashMap<>();
        
        // 进度监控
        AtomicLong processedCount = new AtomicLong(0);
        long totalFiles = shpPaths.size();
        // ❌ 错误：GDAL 不是线程安全的，并行调用会导致崩溃
//        shpPaths.parallelStream().forEach(shpPath -> {
//            DataSource dataSource = ogr.Open(shpPath, 0);  // 多线程同时调用 → 崩溃
//        });
        // 【重要】GDAL 不是线程安全的，必须串行处理文件
        shpPaths.forEach(shpPath -> {
            try {
                System.out.println("开始处理: " + shpPath);
                int featureCount = processSingleShapefile(shpPath, globalGroupMap);
                System.out.println("完成: " + shpPath + " (" + featureCount + " 个 Feature)");
                
                long current = processedCount.incrementAndGet();
                System.out.println("进度: " + current + "/" + totalFiles + " (" + 
                    String.format("%.2f", current * 100.0 / totalFiles) + "%)");
            } catch (Exception e) {
                System.err.println("处理失败: " + shpPath);
                e.printStackTrace();
            }
        });
        
        System.out.println("\n所有文件处理完成，正在排序...");
        
        // 构建最终结果（对每个分组排序）
        return buildFinalResult(globalGroupMap);
    }
    
    /**
     * 处理单个 Shapefile
     */
    private int processSingleShapefile(String shpPath, 
                                      ConcurrentHashMap<String, CopyOnWriteArrayList<String>> globalGroupMap) {
        ogr.RegisterAll();
        DataSource dataSource = null;
        
        try {
            dataSource = ogr.Open(shpPath, 0);
            if (dataSource == null) {
                throw new RuntimeException("无法打开文件: " + shpPath);
            }
            
            Layer layer = dataSource.GetLayer(0);
            if (layer == null) {
                throw new RuntimeException("图层不存在: " + shpPath);
            }
            
            // 批量读取所有 Feature
            List<FeatureRecord> features = readAllFeatures(layer);
            
            // 并行处理当前文件的 Feature
            features.parallelStream().forEach(feature -> {
                String key = buildCoordinateKey(feature.getXList(), feature.getYList());
                
                // 原子操作：如果 key 不存在则创建，然后添加 code
                globalGroupMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                    .add(feature.getCode());
            });
            
            return features.size();
            
        } finally {
            // 释放 GDAL 资源
            if (dataSource != null) {
                dataSource.delete();
            }
        }
    }
    
    /**
     * 读取所有 Feature 到内存
     */
    private List<FeatureRecord> readAllFeatures(Layer layer) {
        List<FeatureRecord> features = new ArrayList<>();
        Feature feature = layer.GetNextFeature();
        
        while (feature != null) {
            String xListStr = Objects.toString(feature.GetFieldAsString("x_slist"), "");
            String yListStr = Objects.toString(feature.GetFieldAsString("y_slist"), "");
            String code = Objects.toString(feature.GetFieldAsString("code"), "");
            
            List<Integer> xList = parseNumberList(xListStr);
            List<Integer> yList = parseNumberList(yListStr);
            
            features.add(new FeatureRecord(xList, yList, code));
            
            feature = layer.GetNextFeature();
        }
        
        return features;
    }
    
    /**
     * 构建最终结果
     */
    private Map<String, List<String>> buildFinalResult(
            ConcurrentHashMap<String, CopyOnWriteArrayList<String>> globalGroupMap) {
        
        Map<String, List<String>> result = new HashMap<>();
        
        globalGroupMap.forEach((key, codes) -> {
            List<String> sortedCodes = new ArrayList<>(codes);
            sortedCodes.sort(Comparator.naturalOrder());
            result.put(key, sortedCodes);
        });
        
        return result;
    }
    
    /**
     * 从字符串中提取数字列表
     */
    private List<Integer> parseNumberList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Integer> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(str);
        
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        
        return numbers;
    }
    
    /**
     * 将数字列表转换为标准化的 Key
     */
    private String buildCoordinateKey(List<Integer> xList, List<Integer> yList) {
        String xStr = xList.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        String yStr = yList.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        return xStr + "|" + yStr;
    }
    
    /**
     * Feature 记录
     */
    private static class FeatureRecord {
        private final List<Integer> xList;
        private final List<Integer> yList;
        private final String code;
        
        public FeatureRecord(List<Integer> xList, List<Integer> yList, String code) {
            this.xList = xList;
            this.yList = yList;
            this.code = code;
        }
        
        public List<Integer> getXList() {
            return xList;
        }
        
        public List<Integer> getYList() {
            return yList;
        }
        
        public String getCode() {
            return code;
        }
    }
}
