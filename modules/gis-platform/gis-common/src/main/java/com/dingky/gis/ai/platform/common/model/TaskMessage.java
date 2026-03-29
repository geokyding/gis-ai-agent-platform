package com.dingky.gis.ai.platform.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 类的功能描述
 * @Author: Tingkyen(home)
 * @Date: 2026/3/29 19:06
 * @Version: V1.0
 */
@Data
public class TaskMessage implements Serializable {
     private String taskId;
     private String filePath;
     private long timestamp;
}
