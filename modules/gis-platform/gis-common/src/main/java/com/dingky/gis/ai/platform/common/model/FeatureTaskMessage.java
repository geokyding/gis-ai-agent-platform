package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FeatureTaskMessage
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 13:57
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureTaskMessage implements Serializable {
    private String taskId;
    private String filePath;
    private Long timestamp;
    private String layerName;
    /**
     * 分片起始点
     */
    private int offSet;
    /**
     * 分片大小：每批处理的数据量
     */
    private int limit;

}
