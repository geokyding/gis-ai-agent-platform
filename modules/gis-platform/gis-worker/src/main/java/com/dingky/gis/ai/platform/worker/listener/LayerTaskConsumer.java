package com.dingky.gis.ai.platform.worker.listener;

import com.dingky.gis.ai.platform.common.model.FeatureTaskMessage;
import com.dingky.gis.ai.platform.common.model.FieldDef;
import com.dingky.gis.ai.platform.common.model.LayerTaskMessage;
import com.dingky.gis.ai.platform.worker.service.GdalService;
import com.dingky.gis.ai.platform.worker.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: LayerTaskConsumer
 * Package: com.dingky.gis.ai.platform.worker.listener
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 14:14
 * @Version 1.0
 **/
@Component
@Slf4j
public class LayerTaskConsumer {
    private final KafkaTemplate<String , Object> kafkaTemplate;
    private final GdalService gdalService;
    private final TableService tableService;
    public LayerTaskConsumer(KafkaTemplate<String , Object> kafkaTemplate, GdalService gdalService, TableService tableService) {
        this.kafkaTemplate = kafkaTemplate;
        this.gdalService = gdalService;
        this.tableService = tableService;
    }

    @KafkaListener(topics = "layer-parse-topic", containerFactory = "layerFactory")
    public void consume(LayerTaskMessage msg){
        log.info("====== 监听到layer-parse-topic任务 ======");
        log.info("收到任务：{}", msg);
        String filePath = msg.getFilePath();
        String layerName = msg.getLayerName();

        // 1获取字段结构
        List<FieldDef> fields = gdalService.getFields(
                msg.getFilePath(),
                msg.getLayerName()
        );

        // 创建表（只执行一次）
        tableService.createTableIfNotExists(msg.getTaskId(), fields);

        long featureCount = gdalService.getFeatureCount(filePath, layerName);
        int limit = 300;
        int batchSize = 0;
        for (int i = 0; i < featureCount; i += limit){
            FeatureTaskMessage featureTaskMessage = new FeatureTaskMessage();
            featureTaskMessage.setTaskId(msg.getTaskId());
            featureTaskMessage.setFilePath(filePath);
            featureTaskMessage.setLayerName(layerName);
            featureTaskMessage.setLimit(limit);
            featureTaskMessage.setOffSet(i);
            featureTaskMessage.setTimestamp(System.currentTimeMillis());
            // 均匀打散
            kafkaTemplate.send("feature-parse-topic", String.valueOf(i), featureTaskMessage);
            batchSize++;
        }
        log.info("分片数量：{}", batchSize);
    }

}
