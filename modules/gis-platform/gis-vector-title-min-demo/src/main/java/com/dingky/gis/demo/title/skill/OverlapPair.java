package com.dingky.gis.demo.title.skill;

/**
 * 重叠对
 */
public class OverlapPair {
    
    /**
     * 集合 A 的 FID
     */
    private long fidA;
    
    /**
     * 集合 B 的 FID
     */
    private long fidB;
    
    /**
     * 集合 A 的 Code
     */
    private String codeA;
    
    /**
     * 集合 B 的 Code
     */
    private String codeB;
    
    /**
     * 代表性 Code（更长的那个）
     */
    private String representativeCode;
    
    /**
     * 代表性 FID
     */
    private long representativeFid;
    
    // Getters and Setters
    
    public long getFidA() {
        return fidA;
    }
    
    public void setFidA(long fidA) {
        this.fidA = fidA;
    }
    
    public long getFidB() {
        return fidB;
    }
    
    public void setFidB(long fidB) {
        this.fidB = fidB;
    }
    
    public String getCodeA() {
        return codeA;
    }
    
    public void setCodeA(String codeA) {
        this.codeA = codeA;
    }
    
    public String getCodeB() {
        return codeB;
    }
    
    public void setCodeB(String codeB) {
        this.codeB = codeB;
    }
    
    public String getRepresentativeCode() {
        return representativeCode;
    }
    
    public void setRepresentativeCode(String representativeCode) {
        this.representativeCode = representativeCode;
    }
    
    public long getRepresentativeFid() {
        return representativeFid;
    }
    
    public void setRepresentativeFid(long representativeFid) {
        this.representativeFid = representativeFid;
    }
    
    @Override
    public String toString() {
        return "OverlapPair{" +
                "fidA=" + fidA +
                ", fidB=" + fidB +
                ", codeA='" + codeA + '\'' +
                ", codeB='" + codeB + '\'' +
                ", representativeCode='" + representativeCode + '\'' +
                '}';
    }
}
