package com.dingky.gis.demo.title;

/**
 * 基本单元数据模型
 * 用于表示从 Shapefile 中提取的要素单元
 */
public class BasicUnit {
    private final long fid;
    private final String code;
    private boolean fromA;
    private String overlapFids;

    public BasicUnit(long fid, String code) {
        this.fid = fid;
        this.code = code;
    }

    public long getFid() {
        return fid;
    }

    public String getCode() {
        return code;
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

    @Override
    public String toString() {
        return "BasicUnit{" +
                "fid=" + fid +
                ", code='" + code + '\'' +
                ", fromA=" + fromA +
                ", overlapFids='" + overlapFids + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicUnit basicUnit = (BasicUnit) o;
        return fid == basicUnit.fid;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(fid);
    }
}
