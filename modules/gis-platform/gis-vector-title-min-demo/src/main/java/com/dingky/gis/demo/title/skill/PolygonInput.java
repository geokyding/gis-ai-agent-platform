package com.dingky.gis.demo.title.skill;

import java.util.List;

/**
 * 多边形输入
 */
public class PolygonInput {
    
    /**
     * 要素唯一标识符
     */
    private long fid;
    
    /**
     * 编码（可能是四叉树编码，也可能是其他格式）
     */
    private String code;
    
    /**
     * 边界框信息（用于四叉树编码转换）
     */
    private BoundingBox bounds;
    
    // Constructors
    
    public PolygonInput() {
    }
    
    public PolygonInput(long fid, String code, BoundingBox bounds) {
        this.fid = fid;
        this.code = code;
        this.bounds = bounds;
    }
    
    // Getters and Setters
    
    public long getFid() {
        return fid;
    }
    
    public void setFid(long fid) {
        this.fid = fid;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BoundingBox getBounds() {
        return bounds;
    }
    
    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }
    
    @Override
    public String toString() {
        return "PolygonInput{" +
                "fid=" + fid +
                ", code='" + code + '\'' +
                ", bounds=" + bounds +
                '}';
    }
}
