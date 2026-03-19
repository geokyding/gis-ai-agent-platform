package com.dingky.gis.ai.vectorcore.vectorparsing.dto.response;

import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ShpParseResponse
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.dto.response
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/19 11:19
 * @Version 1.0
 **/
public class ShpParseResponse {
    private String name;
    private int geomType;
    private String geomTypeName;
    private String srs;           // 建议转成字符串
    private double[] extent;      // [minX, maxX, minY, maxY]
    private long featureCount;
    private Map<String, String> fieldInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGeomType() {
        return geomType;
    }

    public void setGeomType(int geomType) {
        this.geomType = geomType;
    }

    public String getGeomTypeName() {
        return geomTypeName;
    }

    public void setGeomTypeName(String geomTypeName) {
        this.geomTypeName = geomTypeName;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public double[] getExtent() {
        return extent;
    }

    public void setExtent(double[] extent) {
        this.extent = extent;
    }

    public long getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(long featureCount) {
        this.featureCount = featureCount;
    }

    public Map<String, String> getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(Map<String, String> fieldInfo) {
        this.fieldInfo = fieldInfo;
    }
}
