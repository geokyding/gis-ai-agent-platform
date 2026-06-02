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
 * ProjectName: gis-ai-agent-platform
 * ClassName: demo01
 * Package: PACKAGE_NAME
 * Description:
 *
 * @Author: ding
 * @Create 2026/6/2 9:54
 * @Version 1.0
 **/
/**
 * Shapefile 分布式处理器（支持从单机并行平滑升级到 Spark 分布式）
 * 
 * 架构演进路线：
 * - v1.0: 单机串行（当前）
 * - v2.0: 单机并行 + 原子性分组（策略一）
 * - v3.0: Spark 分布式（策略四）
 */
public class demo01 {

    public static void main(String[] args) {
        // 示例：处理多个 Shapefile
        List<String> shpPaths = Arrays.asList(
            "D:\\works\\temp\\data\\partition01.shp",
            "D:\\works\\temp\\data\\partition02.shp"
        );
        
        System.out.println("开始处理 " + shpPaths.size() + " 个 Shapefile...");
        long startTime = System.currentTimeMillis();
        
        // 使用策略一：原子性分组（单机并行）
        Map<String, List<String>> result = processMultipleShapefiles(shpPaths);

        long endTime = System.currentTimeMillis();
        System.out.println("处理完成！耗时: " + (endTime - startTime) + " ms");
        
        // 打印统计信息
        printStatistics(result);
        
        System.out.println("hello world");
    }
    
    /**
     * 打印统计信息
     */
    private static void printStatistics(Map<String, List<String>> result) {
        System.out.println("\n=== 统计信息 ===");
        System.out.println("分组数量: " + result.size());
        System.out.println("总 Code 数: " + result.values().stream().mapToInt(List::size).sum());
        // 打印分组结果
        result.forEach((key, value) -> {
            System.out.println("分组: " + key);
            System.out.println("Code 数量: " + value.size());
            System.out.println("Code 列表: " + value);
        });
    }

    /**
     * 从字符串中提取数字列表
     * 例如: "[ 4665, 655 ]" -> [4665, 655]
     */
    private static List<Integer> parseNumberList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Integer> numbers = new ArrayList<>();
        // 使用正则表达式提取所有数字
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(str);
        
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        
        return numbers;
    }
    
    /**
     * 将数字列表转换为标准化的 Key
     * 例如: [4665, 655] 和 [1578, 425] -> "4665,655|1578,425"
     */
    private static String buildCoordinateKey(List<Integer> xList, List<Integer> yList) {
        String xStr = xList.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        String yStr = yList.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        return xStr + "|" + yStr;
    }
    
    /**
     * 【策略一】处理多个 Shapefile（单机并行 + 原子性分组）
     * 
     * @param shpPaths Shapefile 路径列表
     * @return 分组后的数据，key 为标准化坐标，value 为排序后的 code 列表
     */
    public static Map<String, List<String>> processMultipleShapefiles(List<String> shpPaths) {
        // 全局共享的分组 Map（线程安全）
        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> globalGroupMap = new ConcurrentHashMap<>();
        
        // 进度监控
        AtomicLong processedCount = new AtomicLong(0);
        long totalFiles = shpPaths.size();
        
        // 并行处理每个 Shapefile
        // shpPaths.parallelStream().forEach(
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
     * 
     * @param shpPath Shapefile 路径
     * @param globalGroupMap 全局分组 Map（线程安全）
     * @return 处理的 Feature 数量
     */
    private static int processSingleShapefile(String shpPath, 
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
            
            // 批量读取所有 Feature（减少 GDAL API 调用）
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
    private static List<FeatureRecord> readAllFeatures(Layer layer) {
        List<FeatureRecord> features = new ArrayList<>();
        Feature feature = layer.GetNextFeature();
        final long fid = feature.GetFID();
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
     * 构建最终结果（对每个分组排序）
     */
    private static Map<String, List<String>> buildFinalResult(
            ConcurrentHashMap<String, CopyOnWriteArrayList<String>> globalGroupMap) {
        
        Map<String, List<String>> result = new HashMap<>();
        
        globalGroupMap.forEach((key, codes) -> {
            // 转换为普通 ArrayList 并排序
            List<String> sortedCodes = new ArrayList<>(codes);
            sortedCodes.sort(Comparator.naturalOrder());
            result.put(key, sortedCodes);
        });
        
        return result;
    }
    
    /**
     * 【旧方法】处理单个 Shapefile（保留用于兼容）
     * @deprecated 请使用 {@link #processMultipleShapefiles(List)} 代替
     */
    @Deprecated
    public static Map<String, List<String>> processShapefile() {
        ogr.RegisterAll();
        
        String path = "D:\\works\\temp\\data\\partition01.shp";
        return processMultipleShapefiles(Collections.singletonList(path));
    }
    
    /**
     * Feature 记录（内部类）
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
