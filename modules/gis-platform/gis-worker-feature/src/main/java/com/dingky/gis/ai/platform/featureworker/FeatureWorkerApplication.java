package com.dingky.gis.ai.platform.featureworker;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FeatureWorkerApplication
 * Package: com.dingky.gis.ai.platform.featureworker
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 15:34
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigurationProperties(KafkaPropertiesExt.class)
public class FeatureWorkerApplication {
    public static void main(String[] args){
        SpringApplication.run(FeatureWorkerApplication.class, args);
    }
}
