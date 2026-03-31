package com.dingky.gis.ai.platform.api.controller;

import com.dingky.gis.ai.platform.api.service.KafkaProducerService;
import com.dingky.gis.ai.platform.common.model.TaskMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: UploadController
 * Package: com.dingky.gis.ai.platform.api.controller
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 10:56
 * @Version 1.0
 **/

@RestController
@RequestMapping("/gis")
public class UploadController {
    private final KafkaProducerService producer;
    public UploadController(KafkaProducerService producer) {
        this.producer = producer;
    }
    /**
     * 上传文件接口
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception{
        // 保存文件
        String fileName ="/myhome/data/" + file.getOriginalFilename();
        file.transferTo(new java.io.File(fileName));
        // 构造任务
        TaskMessage message = new TaskMessage();
        message.setTaskId(UUID.randomUUID().toString());
        message.setFilePath(fileName);
        message.setTimestamp(System.currentTimeMillis());

        // 发送任务到kafka
        producer.send(message);
        return "任务已提交："+ message.getTaskId();

    }
}
