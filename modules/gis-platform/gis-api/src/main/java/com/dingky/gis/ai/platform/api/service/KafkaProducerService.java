package com.dingky.gis.ai.platform.api.service;

import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.kafka.topic}")
    private String TOPIC;

    private final KafkaTemplate<String, FileTaskMessage> kafkaTemplate;
    public KafkaProducerService(KafkaTemplate<String, FileTaskMessage> kafkaTemplate) {
        log.info("KafkaProducerService 初始化");
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送任务到Kafka
     */
    public void send(FileTaskMessage message){
        log.info("{}发送{}任务到Kafka: {}", message.getFilePath(), TOPIC, message.getTaskId());
        kafkaTemplate.send(TOPIC, message.getTaskId(), message);
    }
}
