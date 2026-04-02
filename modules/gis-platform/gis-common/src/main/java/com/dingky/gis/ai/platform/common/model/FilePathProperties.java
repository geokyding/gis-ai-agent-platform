package com.dingky.gis.ai.platform.common.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FilePathProperties
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/1 14:06
 * @Version 1.0
 **/
@Data
@ConfigurationProperties(prefix = "app.file")
public class FilePathProperties {
    private String uploadPath;
}
