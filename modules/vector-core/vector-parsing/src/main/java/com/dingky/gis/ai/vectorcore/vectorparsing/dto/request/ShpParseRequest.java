package com.dingky.gis.ai.vectorcore.vectorparsing.dto.request;

import org.springframework.lang.NonNull;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ShpParseRequest
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.dto.request
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/19 10:47
 * @Version 1.0
 **/
public class ShpParseRequest {
    @NonNull
    private String path;

    private Integer srid;

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public Integer getSrid() {
        return srid;
    }

    public void setSrid(Integer srid) {
        this.srid = srid;
    }
}
