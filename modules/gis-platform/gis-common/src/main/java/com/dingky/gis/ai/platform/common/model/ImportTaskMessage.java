package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ImportTaskMessage
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/10 11:16
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskMessage implements Serializable {
    private String taskId;
    private String layerName;
    private List< String> fieldNames;
    private List<FeatureDTO> features;
    private Map<String, String> fieldTypes;
}
