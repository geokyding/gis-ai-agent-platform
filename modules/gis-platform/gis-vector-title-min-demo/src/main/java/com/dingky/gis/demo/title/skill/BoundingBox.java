package com.dingky.gis.demo.title.skill;

/**
 * 边界框
 */
public class BoundingBox {
    
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    
    // Constructors
    
    public BoundingBox() {
    }
    
    public BoundingBox(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    // Methods
    
    /**
     * 获取中心 X 坐标
     */
    public double getCenterX() {
        return (minX + maxX) / 2.0;
    }
    
    /**
     * 获取中心 Y 坐标
     */
    public double getCenterY() {
        return (minY + maxY) / 2.0;
    }
    
    // Getters and Setters
    
    public double getMinX() {
        return minX;
    }
    
    public void setMinX(double minX) {
        this.minX = minX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public void setMinY(double minY) {
        this.minY = minY;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }
    
    @Override
    public String toString() {
        return "BoundingBox{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                '}';
    }
}
