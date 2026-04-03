package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: 类的功能描述
 * @Author: Tingkyen(home)
 * @Date: 2026/3/29 19:06
 * @Version: V1.0
 */
@Data  // getter/setter/toString方法 (lombok 提供)
@NoArgsConstructor  // 无参构造 (lombok 提供)
@AllArgsConstructor  // 全参构造 (lombok 提供)
public class FileTaskMessage implements Serializable {
    // 任务 id（唯一）作用：防止任务重复执行
    private String taskId;
    // 数据文件路径
    private String filePath;
    // 时间戳
    private long timestamp;
}
