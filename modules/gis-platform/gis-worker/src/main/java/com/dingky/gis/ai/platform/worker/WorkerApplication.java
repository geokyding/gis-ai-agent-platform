package com.dingky.gis.ai.platform.worker;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: WorkerApplication
 * Package: com.dingky.gis.ai.platform.worker
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 15:31
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigurationProperties(KafkaPropertiesExt.class)
public class WorkerApplication {
    public static void main(String[] args){
        SpringApplication.run(WorkerApplication.class, args);
    }
}
