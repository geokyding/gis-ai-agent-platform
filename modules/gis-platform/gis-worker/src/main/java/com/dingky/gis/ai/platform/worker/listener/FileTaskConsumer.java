package com.dingky.gis.ai.platform.worker.listener;

import com.dingky.gis.ai.platform.common.model.LayerTaskMessage;
import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import com.dingky.gis.ai.platform.worker.service.GdalService;
import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FileTaskConsumer
 * Package: com.dingky.gis.ai.platform.worker.listener
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/3 14:59
 * @Version 1.0
 **/
@Component
@Slf4j
public class FileTaskConsumer {
    private final KafkaTemplate<String, LayerTaskMessage> kafkaTemplate;
    private final GdalService gdalService;
    public FileTaskConsumer(KafkaTemplate<String, LayerTaskMessage> kafkaTemplate, GdalService gdalService) {
        this.kafkaTemplate = kafkaTemplate;
        this.gdalService = gdalService;
    }
    /**
     * 监听 Kafka 主题
     */
    @KafkaListener(topics = "file-upload-topic",
            containerFactory = "factory")
    public void custom(FileTaskMessage msg){
        log.info("====== 监听到任务 ======");
        log.info("收到任务：{}", msg);
        // 1. 处理任务：解压文件
        log.info("解压文件：{}", msg.getFilePath());
        String filePath = unzipIfNeeded(msg.getFilePath());
        // 获取所有图层名
        List<String> layerNames = gdalService.getLayerNames(filePath);
        // 2.创建任务：图层解析
        for (String layerName : layerNames){
            LayerTaskMessage layerTaskMessage = new LayerTaskMessage();
            layerTaskMessage.setTaskId(msg.getTaskId());
            layerTaskMessage.setFilePath(filePath);
            layerTaskMessage.setLayerName(layerName);
            layerTaskMessage.setTimestamp(String.valueOf(msg.getTimestamp()));
            layerTaskMessage.setLayerType("shp");
            // 3.发送任务到下个订阅“layer-parse-topic”的consumer消费者去切片处理
            kafkaTemplate.send("layer-parse-topic",layerName, layerTaskMessage);
            log.info("发送任务：{}", layerTaskMessage);
        }
    }


    private String unzipIfNeeded(String filePath) {

        return filePath;
    }
}
