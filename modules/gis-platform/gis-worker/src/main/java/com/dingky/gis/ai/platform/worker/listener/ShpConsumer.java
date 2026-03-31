package com.dingky.gis.ai.platform.worker.listener;

import com.dingky.gis.ai.platform.common.model.TaskMessage;
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
public class ShpConsumer {
    /**
     * 监听 Kafka 主题
     */
    @KafkaListener(topics = "shp-topic")
    public void custom(TaskMessage msg){
        System.out.println("====== 收到任务 ======");
        System.out.println("taskId: " + msg.getTaskId());
        System.out.println("filePath: " + msg.getFilePath());
        System.out.println("时间: " + msg.getTimestamp());

        // todo下一步：这里接 GDAL处理逻辑

    }

}
