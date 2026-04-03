package com.dingky.gis.ai.platform.worker.listener;

import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ShpConsumer
 * Package: com.dingky.gis.ai.platform.worker.listener
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 11:35
 * @Version 1.0
 **/
@Component
@Slf4j
public class ShpConsumer {
    /**
     * 监听 Kafka 主题
     */
    @KafkaListener(topics = "layer-parse-topic")
    public void custom(FileTaskMessage msg){
        log.info("====== 收到任务 ======");
        log.info("taskId: {}", msg.getTaskId());
        log.info("filePath: {}", msg.getFilePath());
        log.info("时间: {}", msg.getTimestamp());

        // todo下一步：这里接 GDAL处理逻辑

    }

}
