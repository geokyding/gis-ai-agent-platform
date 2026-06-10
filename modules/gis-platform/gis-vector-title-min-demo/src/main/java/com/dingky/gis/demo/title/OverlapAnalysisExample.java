package com.dingky.gis.demo.title;

import java.util.*;

/**
 * 重叠分析使用示例
 * 
 * 演示如何使用 OverlapAnalyzer 进行 Shapefile 重叠分析
 */
public class OverlapAnalysisExample {
    
    public static void main(String[] args) {
        // 示例1: 基本用法
        basicUsage();
        
        // 示例2: 优化算法对比
        optimizedAlgorithmComparison();
    }
    
    /**
     * 基本用法示例
     */
    private static void basicUsage() {
        System.out.println("=== 示例1: 基本用法 ===\n");
        
        // 模拟数据：同一坐标位置的多个单元
        List<BasicUnit> units = Arrays.asList(
            new BasicUnit(1, "A001"),
            new BasicUnit(2, "A001-1"),
            new BasicUnit(3, "A002"),
            new BasicUnit(4, "B001"),
            new BasicUnit(5, "B001-1")
        );
        
        // 设置来源标记（模拟两个不同的 Shapefile）
        units.get(0).setFromA(true);   // A001 来自文件A
        units.get(1).setFromA(false);  // A001-1 来自文件B
        units.get(2).setFromA(true);   // A002 来自文件A
        units.get(3).setFromA(true);   // B001 来自文件A
        units.get(4).setFromA(false);  // B001-1 来自文件B
        
        // 按 code 排序
        units.sort(Comparator.comparing(BasicUnit::getCode));
        
        System.out.println("输入数据:");
        units.forEach(u -> System.out.println("  FID=" + u.getFid() + 
                                             ", Code=" + u.getCode() + 
                                             ", FromA=" + u.isFromA()));
        
        // 查找重叠
        List<BasicUnit> overlaps = OverlapAnalyzer.findOverlap(units);
        
        System.out.println("\n重叠结果:");
        overlaps.forEach(u -> System.out.println("  FID=" + u.getFid() + 
                                               ", Code=" + u.getCode() + 
                                               ", OverlapFIDs=" + u.getOverlapFids()));
    }
    
    /**
     * 优化算法对比
     */
    private static void optimizedAlgorithmComparison() {
        System.out.println("\n\n=== 示例2: 优化算法对比 ===\n");
        
        // 创建测试数据
        List<BasicUnit> units = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            BasicUnit unit = new BasicUnit(i, "CODE" + String.format("%03d", i));
            unit.setFromA(i % 2 == 0);  // 偶数来自A，奇数来自B
            units.add(unit);
        }
        
        units.sort(Comparator.comparing(BasicUnit::getCode));
        
        // 原始算法
        long start1 = System.currentTimeMillis();
        List<BasicUnit> result1 = OverlapAnalyzer.findOverlap(units);
        long time1 = System.currentTimeMillis() - start1;
        
        // 优化算法
        long start2 = System.currentTimeMillis();
        List<BasicUnit> result2 = OverlapAnalyzer.findOverlapOptimized(units);
        long time2 = System.currentTimeMillis() - start2;
        
        System.out.println("数据量: " + units.size() + " 个单元");
        System.out.println("原始算法耗时: " + time1 + " ms, 找到 " + result1.size() + " 个重叠");
        System.out.println("优化算法耗时: " + time2 + " ms, 找到 " + result2.size() + " 个重叠");
        System.out.println("性能提升: " + (time1 > 0 ? (double)time1 / time2 : "N/A") + "x");
    }
}
