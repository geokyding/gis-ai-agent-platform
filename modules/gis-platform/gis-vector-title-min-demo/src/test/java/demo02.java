import ch.qos.logback.core.joran.conditional.IfAction;
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
 * ClassName: demo02
 * Package: PACKAGE_NAME
 * Description:
 *
 * @Author: ding
 * @Create 2026/6/2 15:43
 * @Version 1.0
 **/
public class demo02 {

    public static void main(String[] args) {
        // 示例：处理多个 Shapefile
        List<String> shpPaths = Arrays.asList(
                "D:\\works\\temp\\data\\partition01.shp",
                "D:\\works\\temp\\data\\partition02.shp"
        );

        System.out.println("开始处理 " + shpPaths.size() + " 个 Shapefile...");
        long startTime = System.currentTimeMillis();

        // 使用策略一：原子性分组（单机并行）
        Map<String, List<BasicUnit>> result = processMultipleShapefiles(shpPaths);
        
        // 获取重叠区域
        Map<String, List<BasicUnit>> overlappingUnits = findOverlappingUnits(result);

        long endTime = System.currentTimeMillis();
        System.out.println("处理完成！耗时: " + (endTime - startTime) + " ms");

        // 打印统计信息
        printStatistics(overlappingUnits);

        System.out.println("hello world");
    }

    private static void printStatistics(Map<String, List<BasicUnit>> result) {
        System.out.println("重叠结果");
        result.forEach((String key, List<BasicUnit> units)-> {
            System.out.println("网格："+key+"; codes: "+getCodesByList(units));
        });
    }

    private static String getCodesByList(List<BasicUnit> units) {
        units.sort(Comparator.comparing(BasicUnit::getCode));
        return units.stream()
                .map(BasicUnit::getCode)
                .collect(Collectors.joining(","));
    }

    private static Map<String, List<BasicUnit>> findOverlappingUnits(Map<String, List<BasicUnit>> map) {
        Map<String, List<BasicUnit>> overlappingUnits = new HashMap<>();
        // 对每个list计算对应的重叠区域
        map.forEach((key, units) -> {
            overlappingUnits.put(key, findOverlap(units));
        });
        return overlappingUnits;
    }
    /**
     * 处理单个分组的 BasicUnit 列表，找出重叠单元
     * 
     * @param units 已排序的 BasicUnit 列表
     * @return 重叠的 BasicUnit 集合
     */
    private static List<BasicUnit> findOverlap(List<BasicUnit> units) {
        Set<BasicUnit> result = new HashSet<>();
        
        for (int i = 0; i < units.size(); i++) {
            BasicUnit current = units.get(i);
            
            for (int j = i + 1; j < units.size(); j++) {
                BasicUnit next = units.get(j);
                
                // 如果 next.code 不以 current.code 开头，说明已经超出当前前缀范围
                if (!next.getCode().startsWith(current.getCode())) {
                    // 【关键】设置为 j-1，因为外层循环会执行 i++，最终 i=j
                    // 使用 break 而不是 continue，直接跳出内层循环
                    // 外层循环会执行 i++，所以设置为 j-1 才能让下次从 j 开始
                    i = j - 1;
                    break;
                }
                
                // 如果 fromA 不同，取 code 更长的那个
                if (current.fromA != next.fromA) {
                    BasicUnit unit = current.getCode().length() > next.getCode().length() ? current : next;
                    unit.setOverlapFids(current.getKey()+","+next.getKey());
                    result.add(unit);
                }
            }
        }
        
        return new ArrayList<>(result);
    }

    private static Map<String, List<BasicUnit>> processMultipleShapefiles(List<String> shpPaths) {
        // 全局共享的分组 Map（线程安全）
        ConcurrentHashMap<String, CopyOnWriteArrayList<BasicUnit>> globalGroupMap = new ConcurrentHashMap<>();

        // 进度监控
        AtomicLong processedCount = new AtomicLong(0);
        long totalFiles = shpPaths.size();
        String refShpPath = shpPaths.get(0);
        shpPaths.forEach(shpPath -> {
            try {
                System.out.println("正在处理: " + shpPath);
                boolean fromA =  shpPath.equals(refShpPath);
                int featureCount = processShapefile(shpPath, globalGroupMap, fromA);
                System.out.println("完成: " + shpPath + " (" + featureCount + " 个 Feature)");

                long current = processedCount.incrementAndGet();
                System.out.println("进度: " + current + "/" + totalFiles + " (" +
                        String.format("%.2f", current * 100.0 / totalFiles) + "%)");
            }catch (Exception e){
                System.err.println("处理失败: " + shpPath);
                e.printStackTrace();
            }

        });
        return buildFinalResult(globalGroupMap);
    }

    /**
     * 构建最终结果（对每个分组排序）
     */
    private static Map<String, List<BasicUnit>> buildFinalResult(ConcurrentHashMap<String, CopyOnWriteArrayList<BasicUnit>> globalGroupMap) {
        Map<String, List<BasicUnit>> result = new HashMap<>();
        
        globalGroupMap.forEach((key, units) -> {
            // 转换为普通 ArrayList 并按 code 排序
            List<BasicUnit> sortedUnits = new ArrayList<>(units);
            sortedUnits.sort(Comparator.comparing(BasicUnit::getCode));
            result.put(key, sortedUnits);
        });
        
        return result;
    }

    private static int processShapefile(String shpPath, ConcurrentHashMap<String, CopyOnWriteArrayList<BasicUnit>> globalGroupMap, boolean fromA) {
        ogr.RegisterAll();
        DataSource dataSource = null;
        int featureCount = 0;
        try {
            dataSource = ogr.Open(shpPath, 0);
            if (dataSource == null) {
                System.err.println("无法打开 Shapefile: " + shpPath);
                return 0;
            }
            Layer layer = dataSource.GetLayer(0);
            if (layer == null) {
                System.err.println("无法打开图层: " + shpPath);
            }
            Feature feature = layer.GetNextFeature();


            while (feature != null) {
                String xSlist = Objects.toString(feature.GetFieldAsString("x_slist"), "");
                String ySlist = Objects.toString(feature.GetFieldAsString("y_slist"), "");
                String code = feature.GetFieldAsString("code");
                // xSlist + "|" + ySlist;
                String key = buildCoordinateKey(parseNumberList(xSlist), parseNumberList(ySlist));
                BasicUnit unit = new BasicUnit(feature.GetFID(), code);
                unit.setFromA(fromA);
                globalGroupMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(unit);
                featureCount++;
                feature = layer.GetNextFeature();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return featureCount;
    }

    private static class BasicUnit {
        private final long fid;
        private final String code;
        private boolean fromA;
        private String overlapFids;

        public BasicUnit(long fid, String code) {
            this.fid = fid;
            this.code = code;
        }

        public long getKey() {
            return fid;
        }

        public String getCode() {
            return code;
        }
        @Override
        public String toString() {
            return  getCode();
        }

        public boolean isFromA() {
            return fromA;
        }

        public void setFromA(boolean fromA) {
            this.fromA = fromA;
        }

        public String getOverlapFids() {
            return overlapFids;
        }

        public void setOverlapFids(String overlapFids) {
            this.overlapFids = overlapFids;
        }
    }

    // 优化思路：利用已排序的特性，按前缀分组后批量处理
    private static List<BasicUnit> processListBasicUnitsOptimized(List<BasicUnit> units) {
        Set<BasicUnit> result = new HashSet<>();

        // 按前缀分组
        Map<String, List<BasicUnit>> prefixGroups = new LinkedHashMap<>();
        for (BasicUnit unit : units) {
            String prefix = unit.getCode().substring(0, Math.min(3, unit.getCode().length()));
            prefixGroups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(unit);
        }

        // 对每个前缀组处理
        for (List<BasicUnit> group : prefixGroups.values()) {
            if (group.size() > 1) {
                // 检查 fromA 是否不同
                boolean hasA = group.stream().anyMatch(u -> u.fromA);
                boolean hasB = group.stream().anyMatch(u -> !u.fromA);

                if (hasA && hasB) {
                    // 取 code 最长的
                    BasicUnit longest = group.stream()
                            .max(Comparator.comparingInt(u -> u.getCode().length()))
                            .orElse(null);
                    if (longest != null) {
                        result.add(longest);
                    }
                }
            }
        }


        return new ArrayList<>(result);
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


}
