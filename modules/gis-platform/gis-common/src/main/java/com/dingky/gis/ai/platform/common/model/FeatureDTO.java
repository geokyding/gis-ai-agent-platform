package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FeatureDTO
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/10 11:18
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDTO implements Serializable {
    private Long fid;
    /**
     * 几何（Byte数组）
     */
    private byte[] geometry;
    /**
     * 属性（简化：JSON字符串）
     */
    private Map<String, Object> properties;
//    private List<String> fieldNames;
}
