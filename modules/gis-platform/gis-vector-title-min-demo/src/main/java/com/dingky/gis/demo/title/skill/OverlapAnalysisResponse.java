package com.dingky.gis.demo.title.skill;

import java.util.List;

/**
 * 重叠分析响应
 */
public class OverlapAnalysisResponse {
    
    /**
     * 重叠网格数量
     */
    private int totalOverlapGrids;
    
    /**
     * 重叠单元总数
     */
    private int totalOverlapUnits;
    
    /**
     * 详细结果列表
     */
    private List<OverlapDetail> details;
    
    // Getters and Setters
    
    public int getTotalOverlapGrids() {
        return totalOverlapGrids;
    }
    
    public void setTotalOverlapGrids(int totalOverlapGrids) {
        this.totalOverlapGrids = totalOverlapGrids;
    }
    
    public int getTotalOverlapUnits() {
        return totalOverlapUnits;
    }
    
    public void setTotalOverlapUnits(int totalOverlapUnits) {
        this.totalOverlapUnits = totalOverlapUnits;
    }
    
    public List<OverlapDetail> getDetails() {
        return details;
    }
    
    public void setDetails(List<OverlapDetail> details) {
        this.details = details;
    }
    
    @Override
    public String toString() {
        return "OverlapAnalysisResponse{" +
                "totalOverlapGrids=" + totalOverlapGrids +
                ", totalOverlapUnits=" + totalOverlapUnits +
                ", details=" + (details != null ? details.size() : 0) + "条" +
                '}';
    }
}
