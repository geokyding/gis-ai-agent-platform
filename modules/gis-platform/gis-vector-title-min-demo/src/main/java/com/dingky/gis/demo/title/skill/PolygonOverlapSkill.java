package com.dingky.gis.demo.title.skill;

import com.dingky.gis.demo.title.BasicUnit;
import com.dingky.gis.demo.title.OverlapAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多边形重叠分析 Skill
 * 
 * 功能：
 * 1. 检查输入是否为四叉树编码格式
 * 2. 如果不是，调用转换接口转换为四叉树编码
 * 3. 执行重叠分析
 * 4. 返回重叠结果
 */
public class PolygonOverlapSkill {
    
    /**
     * 执行多边形重叠分析
     * 
     * @param request 重叠分析请求
     * @return 重叠分析结果
     */
    public OverlapAnalysisResponse execute(OverlapAnalysisRequest request) {
        // Step 1: 验证输入
        validateRequest(request);
        
        // Step 2: 检查并转换四叉树编码
        List<BasicUnit> unitsA = ensureQuadtreeEncoding(request.getPolygonSetA(), "A");
        List<BasicUnit> unitsB = ensureQuadtreeEncoding(request.getPolygonSetB(), "B");
        
        System.out.println("✓ 四叉树编码检查完成");
        System.out.println("  - 集合 A: " + unitsA.size() + " 个单元");
        System.out.println("  - 集合 B: " + unitsB.size() + " 个单元");
        
        // Step 3: 按坐标分组（使用四叉树编码前缀）
        Map<String, List<BasicUnit>> groupedUnits = groupByQuadtreePrefix(unitsA, unitsB);
        
        System.out.println("✓ 分组完成，共 " + groupedUnits.size() + " 个网格");
        
        // Step 4: 查找重叠
        Map<String, List<BasicUnit>> overlappingUnits = OverlapAnalyzer.findOverlappingUnits(groupedUnits);
        
        // Step 5: 构建响应
        return buildResponse(overlappingUnits);
    }
    
    /**
     * 确保数据为四叉树编码格式
     * 
     * @param polygons 多边形列表
     * @param setName 集合名称（用于日志）
     * @return 转换后的 BasicUnit 列表
     */
    private List<BasicUnit> ensureQuadtreeEncoding(List<PolygonInput> polygons, String setName) {
        List<BasicUnit> units = new ArrayList<>();
        int convertedCount = 0;
        
        for (PolygonInput polygon : polygons) {
            String code = polygon.getCode();
            
            // 检查是否为四叉树编码
            if (!isQuadtreeEncoded(code)) {
                System.out.println("⚠️  检测到非四叉树编码，正在转换...");
                
                // 调用转换接口
                String quadtreeCode = convertToQuadtree(polygon);
                convertedCount++;
                
                BasicUnit unit = new BasicUnit(polygon.getFid(), quadtreeCode);
                unit.setFromA("A".equals(setName));
                units.add(unit);
            } else {
                BasicUnit unit = new BasicUnit(polygon.getFid(), code);
                unit.setFromA("A".equals(setName));
                units.add(unit);
            }
        }
        
        if (convertedCount > 0) {
            System.out.println("  - 集合 " + setName + ": 已转换 " + convertedCount + " 个要素为四叉树编码");
        }
        
        return units;
    }
    
    /**
     * 检查是否为四叉树编码
     * 
     * 四叉树编码特征：
     * - 由数字 0-3 组成
     * - 长度通常为偶数（表示层级）
     * - 可能包含分隔符（如 "-" 或 "/"）
     * 
     * @param code 待检查的编码
     * @return true 如果是四叉树编码
     */
    private boolean isQuadtreeEncoded(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        // 移除可能的分隔符
        String cleaned = code.replaceAll("[-/]", "");
        
        // 检查是否只包含 0-3
        return cleaned.matches("[0-3]+");
    }
    
    /**
     * 将普通几何转换为四叉树编码
     * 
     * TODO: 集成实际的四叉树编码转换服务
     * 
     * @param polygon 多边形输入
     * @return 四叉树编码
     */
    private String convertToQuadtree(PolygonInput polygon) {
        // 方案1: 调用外部 API
        // return QuadtreeService.encode(polygon.getBounds());
        
        // 方案2: 基于边界框计算四叉树编码
        if (polygon.getBounds() != null) {
            return calculateQuadtreeCode(polygon.getBounds());
        }
        
        // 方案3: 降级处理 - 使用 FID 作为临时编码
        System.err.println("警告: 无法生成四叉树编码，使用 FID 作为临时编码");
        return "TEMP_" + polygon.getFid();
    }
    
    /**
     * 根据边界框计算四叉树编码
     * 
     * @param bounds 边界框
     * @return 四叉树编码
     */
    private String calculateQuadtreeCode(BoundingBox bounds) {
        // 简化的四叉树编码算法
        // 实际项目中应替换为专业的四叉树库
        
        StringBuilder quadtreeCode = new StringBuilder();
        
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        
        // 全局范围（需要根据实际坐标系调整）
        double minX = -180.0, maxX = 180.0;
        double minY = -90.0, maxY = 90.0;
        
        // 递归细分 8 层
        int levels = 8;
        for (int i = 0; i < levels; i++) {
            double midX = (minX + maxX) / 2.0;
            double midY = (minY + maxY) / 2.0;
            
            int quadrant;
            if (centerX >= midX && centerY >= midY) {
                quadrant = 1; // 右上
            } else if (centerX < midX && centerY >= midY) {
                quadrant = 0; // 左上
            } else if (centerX < midX && centerY < midY) {
                quadrant = 2; // 左下
            } else {
                quadrant = 3; // 右下
            }
            
            quadtreeCode.append(quadrant);
            
            // 更新边界
            if (centerX >= midX) minX = midX; else maxX = midX;
            if (centerY >= midY) minY = midY; else maxY = midY;
        }
        
        return quadtreeCode.toString();
    }
    
    /**
     * 按四叉树编码前缀分组
     * 
     * @param unitsA 集合 A
     * @param unitsB 集合 B
     * @return 分组后的单元
     */
    private Map<String, List<BasicUnit>> groupByQuadtreePrefix(List<BasicUnit> unitsA, List<BasicUnit> unitsB) {
        Map<String, List<BasicUnit>> grouped = new HashMap<>();
        
        // 合并两个集合并按前缀分组
        List<BasicUnit> allUnits = new ArrayList<>();
        allUnits.addAll(unitsA);
        allUnits.addAll(unitsB);
        
        for (BasicUnit unit : allUnits) {
            // 提取四叉树编码的前缀作为分组键
            // 例如: "0123" -> 取前 2 位 "01" 作为网格键
            String prefix = extractQuadtreePrefix(unit.getCode());
            
            grouped.computeIfAbsent(prefix, k -> new ArrayList<>()).add(unit);
        }
        
        return grouped;
    }
    
    /**
     * 提取四叉树编码前缀
     * 
     * @param quadtreeCode 四叉树编码
     * @return 前缀（网格键）
     */
    private String extractQuadtreePrefix(String quadtreeCode) {
        if (quadtreeCode == null || quadtreeCode.length() < 2) {
            return quadtreeCode != null ? quadtreeCode : "";
        }
        
        // 取前 2-4 位作为网格键（可根据精度需求调整）
        int prefixLength = Math.min(4, quadtreeCode.length());
        return quadtreeCode.substring(0, prefixLength);
    }
    
    /**
     * 构建响应
     * 
     * @param overlappingUnits 重叠单元
     * @return 分析结果
     */
    private OverlapAnalysisResponse buildResponse(Map<String, List<BasicUnit>> overlappingUnits) {
        OverlapAnalysisResponse response = new OverlapAnalysisResponse();
        
        List<OverlapDetail> details = new ArrayList<>();
        int totalOverlapUnits = 0;
        
        for (Map.Entry<String, List<BasicUnit>> entry : overlappingUnits.entrySet()) {
            String gridKey = entry.getKey();
            List<BasicUnit> units = entry.getValue();
            
            OverlapDetail detail = new OverlapDetail();
            detail.setGridKey(gridKey);
            
            // 提取 codes 和 fids
            List<String> codes = units.stream()
                .map(BasicUnit::getCode)
                .collect(Collectors.toList());
            List<Long> fids = units.stream()
                .map(BasicUnit::getFid)
                .collect(Collectors.toList());
            
            detail.setCodes(codes);
            detail.setFids(fids);
            
            // 找出重叠对（来自不同集合的单元）
            List<BasicUnit> fromA = units.stream()
                .filter(BasicUnit::isFromA)
                .collect(Collectors.toList());
            List<BasicUnit> fromB = units.stream()
                .filter(u -> !u.isFromA())
                .collect(Collectors.toList());
            
            List<OverlapPair> overlapPairs = new ArrayList<>();
            for (BasicUnit a : fromA) {
                for (BasicUnit b : fromB) {
                    OverlapPair pair = new OverlapPair();
                    pair.setFidA(a.getFid());
                    pair.setFidB(b.getFid());
                    pair.setCodeA(a.getCode());
                    pair.setCodeB(b.getCode());
                    
                    // 选择更长的 code 作为代表性编码
                    if (a.getCode().length() >= b.getCode().length()) {
                        pair.setRepresentativeCode(a.getCode());
                        pair.setRepresentativeFid(a.getFid());
                    } else {
                        pair.setRepresentativeCode(b.getCode());
                        pair.setRepresentativeFid(b.getFid());
                    }
                    
                    overlapPairs.add(pair);
                }
            }
            
            detail.setOverlapPairs(overlapPairs);
            details.add(detail);
            
            totalOverlapUnits += units.size();
        }
        
        response.setTotalOverlapGrids(overlappingUnits.size());
        response.setTotalOverlapUnits(totalOverlapUnits);
        response.setDetails(details);
        
        return response;
    }
    
    /**
     * 验证请求
     */
    private void validateRequest(OverlapAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getPolygonSetA() == null || request.getPolygonSetA().isEmpty()) {
            throw new IllegalArgumentException("多边形集合 A 不能为空");
        }
        if (request.getPolygonSetB() == null || request.getPolygonSetB().isEmpty()) {
            throw new IllegalArgumentException("多边形集合 B 不能为空");
        }
    }
}
