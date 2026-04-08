package com.dingky.gis.ai.platform.featureworker.listener;

import com.dingky.gis.ai.platform.common.model.FeatureTaskMessage;
import com.dingky.gis.ai.platform.featureworker.service.GdalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FeatureTaskConsumer
 * Package: com.dingky.gis.ai.platform.featureworker.listener
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 16:31
 * @Version 1.0
 **/
@Component
@Slf4j
public class FeatureTaskConsumer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GdalService gdalService;
    public FeatureTaskConsumer(KafkaTemplate<String, Object> kafkaTemplate, GdalService gdalService) {
        this.kafkaTemplate = kafkaTemplate;
        this.gdalService = gdalService;
    }
    @KafkaListener(topics = "feature-parse-topic", containerFactory = "kafkaListenerContainerFactory")
    public void consume(FeatureTaskMessage msg){
        log.info("====== 监听到feature-parse-topic任务 ======");
        log.info("收到任务：{}", msg);
        // 1. 处理任务：切片
        log.info("开始切片：{}", msg);
        List<Long> features =  gdalService.readFeature(msg.getFilePath(), msg.getLayerName(), msg.getOffSet(), msg.getLimit());
        // 2. 发送任务到下个订阅“import-feature-topic”的consumer消费者去切片处理
        kafkaTemplate.send("import-feature-topic", msg.getTaskId(), "待开发feature-import-topic");
        log.info("发送任务：{}", msg);
    }
}
