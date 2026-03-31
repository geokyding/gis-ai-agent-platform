package com.dingky.gis.ai.platform.api;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: UploadApplication
 * Package: com.dingky.gis.ai.platform.api
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 11:08
 * @Version 1.0
 **/
@SpringBootApplication(scanBasePackages = {"com.dingky.gis.ai.platform.api"})
@EnableConfigurationProperties(KafkaPropertiesExt.class)
public class UploadApplication {
    public static void main(String[] args){
        SpringApplication.run(UploadApplication.class, args);
    }
}
