package com.dingky.gis.ai.platform.api.service;

import com.dingky.gis.ai.platform.common.model.TaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaProducerService
 * Package: com.dingky.gis.ai.platform.api.service
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 10:49
 * @Version 1.0
 **/

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, TaskMessage> kafkaTemplate;
    public KafkaProducerService(KafkaTemplate<String, TaskMessage> kafkaTemplate) {
        log.info("KafkaProducerService 初始化");
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送任务到Kafka
     */
    public void send(TaskMessage message){
        System.out.println("发送任务到Kafka: " + message.getTaskId());
        kafkaTemplate.send("shp-topic", message);
    }
}
