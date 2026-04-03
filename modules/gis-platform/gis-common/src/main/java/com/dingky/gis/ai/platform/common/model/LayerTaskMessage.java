package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: LayerTaskMessage
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/3 14:28
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LayerTaskMessage implements Serializable {
    private String taskId;
    private String filePath;
    private String timestamp;
    private String layerName;
    private String layerType;
}
