package com.dingky.gis.demo.title.skill;

import java.util.List;

/**
 * 重叠分析请求
 */
public class OverlapAnalysisRequest {
    
    /**
     * 多边形集合 A（例如：行政区划）
     */
    private List<PolygonInput> polygonSetA;
    
    /**
     * 多边形集合 B（例如：网格划分）
     */
    private List<PolygonInput> polygonSetB;
    
    /**
     * 是否包含详细信息
     */
    private boolean includeDetails = true;
    
    // Getters and Setters
    
    public List<PolygonInput> getPolygonSetA() {
        return polygonSetA;
    }
    
    public void setPolygonSetA(List<PolygonInput> polygonSetA) {
        this.polygonSetA = polygonSetA;
    }
    
    public List<PolygonInput> getPolygonSetB() {
        return polygonSetB;
    }
    
    public void setPolygonSetB(List<PolygonInput> polygonSetB) {
        this.polygonSetB = polygonSetB;
    }
    
    public boolean isIncludeDetails() {
        return includeDetails;
    }
    
    public void setIncludeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
    }
    
    @Override
    public String toString() {
        return "OverlapAnalysisRequest{" +
                "polygonSetA=" + (polygonSetA != null ? polygonSetA.size() : 0) + "个要素" +
                ", polygonSetB=" + (polygonSetB != null ? polygonSetB.size() : 0) + "个要素" +
                ", includeDetails=" + includeDetails +
                '}';
    }
}
