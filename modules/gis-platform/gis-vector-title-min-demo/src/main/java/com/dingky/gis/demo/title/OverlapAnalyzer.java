package com.dingky.gis.demo.title;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 重叠区域分析器
 * 
 * 功能：
 * 1. 分析两个 Shapefile 中相同坐标位置的要素
 * 2. 找出具有相同前缀但来源不同的重叠单元
 * 3. 标记重叠单元的 FID 信息
 */
public class OverlapAnalyzer {
    
    /**
     * 查找所有分组中的重叠单元
     * 
     * @param groupedUnits 按坐标分组的单元 Map
     * @return 每个分组的重叠单元列表
     */
    public static Map<String, List<BasicUnit>> findOverlappingUnits(Map<String, List<BasicUnit>> groupedUnits) {
        Map<String, List<BasicUnit>> overlappingUnits = new HashMap<>();
        
        groupedUnits.forEach((key, units) -> {
            List<BasicUnit> overlaps = findOverlap(units);
            if (!overlaps.isEmpty()) {
                overlappingUnits.put(key, overlaps);
            }
        });
        
        return overlappingUnits;
    }
    
    /**
     * 在单个分组中查找重叠单元
     * 
     * 算法逻辑：
     * 1. 遍历已排序的单元列表
     * 2. 比较具有相同前缀的单元
     * 3. 如果 fromA 不同，标记为重叠，取 code 更长的那个
     * 
     * @param units 已排序的 BasicUnit 列表
     * @return 重叠的 BasicUnit 列表
     */
    public static List<BasicUnit> findOverlap(List<BasicUnit> units) {
        Set<BasicUnit> result = new HashSet<>();
        
        for (int i = 0; i < units.size(); i++) {
            BasicUnit current = units.get(i);
            
            for (int j = i + 1; j < units.size(); j++) {
                BasicUnit next = units.get(j);
                
                // 如果 next.code 不以 current.code 开头，说明已经超出当前前缀范围
                if (!next.getCode().startsWith(current.getCode())) {
                    // 设置为 j-1，因为外层循环会执行 i++，最终 i=j
                    i = j - 1;
                    break;
                }
                
                // 如果 fromA 不同，取 code 更长的那个
                if (current.isFromA() != next.isFromA()) {
                    BasicUnit unit = current.getCode().length() > next.getCode().length() ? current : next;
                    unit.setOverlapFids(current.getFid() + "," + next.getFid());
                    result.add(unit);
                }
            }
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * 优化的重叠查找算法（按前缀分组后批量处理）
     * 时间复杂度：O(n) vs 原始 O(n²)
     * 
     * @param units 已排序的 BasicUnit 列表
     * @return 重叠的 BasicUnit 列表
     */
    public static List<BasicUnit> findOverlapOptimized(List<BasicUnit> units) {
        Set<BasicUnit> result = new HashSet<>();
        
        // 按前缀分组（取前3位作为前缀）
        Map<String, List<BasicUnit>> prefixGroups = new LinkedHashMap<>();
        for (BasicUnit unit : units) {
            String prefix = unit.getCode().substring(0, Math.min(3, unit.getCode().length()));
            prefixGroups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(unit);
        }
        
        // 对每个前缀组处理
        for (List<BasicUnit> group : prefixGroups.values()) {
            if (group.size() > 1) {
                // 检查 fromA 是否不同
                boolean hasA = group.stream().anyMatch(BasicUnit::isFromA);
                boolean hasB = group.stream().anyMatch(u -> !u.isFromA());
                
                if (hasA && hasB) {
                    // 取 code 最长的
                    BasicUnit longest = group.stream()
                        .max(Comparator.comparingInt(u -> u.getCode().length()))
                        .orElse(null);
                    
                    if (longest != null) {
                        // 标记所有参与重叠的 FID
                        String overlapFids = group.stream()
                            .map(u -> String.valueOf(u.getFid()))
                            .collect(Collectors.joining(","));
                        longest.setOverlapFids(overlapFids);
                        result.add(longest);
                    }
                }
            }
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * 格式化输出重叠结果
     * 
     * @param overlappingUnits 重叠单元 Map
     */
    public static void printOverlapStatistics(Map<String, List<BasicUnit>> overlappingUnits) {
        System.out.println("\n=== 重叠分析结果 ===");
        System.out.println("重叠网格数量: " + overlappingUnits.size());
        System.out.println("总重叠单元数: " + overlappingUnits.values().stream()
            .mapToInt(List::size)
            .sum());
        
        overlappingUnits.forEach((key, units) -> {
            System.out.println("\n网格: " + key);
            units.sort(Comparator.comparing(BasicUnit::getCode));
            units.forEach(unit -> {
                System.out.println("  - Code: " + unit.getCode() + 
                                 ", FID: " + unit.getFid() + 
                                 ", FromA: " + unit.isFromA() +
                                 ", OverlapFIDs: " + unit.getOverlapFids());
            });
        });
    }
}
