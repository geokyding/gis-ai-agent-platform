package com.dingky.gis.ai.platform.importworker;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: App
 * Package: com.dingky.gis.ai.platform.importworker
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/10 15:17
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigurationProperties(KafkaPropertiesExt.class)
public class App {
    public static void main(String[] args){
        SpringApplication.run(App.class, args);
    }
}
