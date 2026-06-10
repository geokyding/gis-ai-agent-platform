package com.dingky.gis.demo.title.skill;

import java.util.List;

/**
 * 重叠详情
 */
public class OverlapDetail {
    
    /**
     * 网格键（四叉树编码前缀）
     */
    private String gridKey;
    
    /**
     * 该网格内的所有 code
     */
    private List<String> codes;
    
    /**
     * 该网格内的所有 FID
     */
    private List<Long> fids;
    
    /**
     * 重叠对列表
     */
    private List<OverlapPair> overlapPairs;
    
    // Getters and Setters
    
    public String getGridKey() {
        return gridKey;
    }
    
    public void setGridKey(String gridKey) {
        this.gridKey = gridKey;
    }
    
    public List<String> getCodes() {
        return codes;
    }
    
    public void setCodes(List<String> codes) {
        this.codes = codes;
    }
    
    public List<Long> getFids() {
        return fids;
    }
    
    public void setFids(List<Long> fids) {
        this.fids = fids;
    }
    
    public List<OverlapPair> getOverlapPairs() {
        return overlapPairs;
    }
    
    public void setOverlapPairs(List<OverlapPair> overlapPairs) {
        this.overlapPairs = overlapPairs;
    }
    
    @Override
    public String toString() {
        return "OverlapDetail{" +
                "gridKey='" + gridKey + '\'' +
                ", codes=" + codes +
                ", fids=" + fids +
                ", overlapPairs=" + (overlapPairs != null ? overlapPairs.size() : 0) +
                '}';
    }
}
