package com.dingky.gis.ai.platform.api.controller;

import com.dingky.gis.ai.platform.api.service.KafkaProducerService;
import com.dingky.gis.ai.platform.common.model.TaskMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
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
    private final Logger log = org.slf4j.LoggerFactory.getLogger(UploadController.class);
    @Value("${app.file.upload-path}")
    private String uploadPath;
    private final KafkaProducerService producer;
    public UploadController(KafkaProducerService producer) {
        log.info("UploadController 初始化");
        this.producer = producer;
    }
    /**
     * 上传文件接口
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception{
        String dataPath = LocalDate.now().toString();
        String dir = uploadPath + File.separator + dataPath;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        // 保存文件
        String fileName =dir + File.separator + file.getOriginalFilename();
        file.transferTo(new java.io.File(fileName));
        log.info("上传文件：{}", fileName);
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
