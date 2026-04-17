package com.dingky.gis.ai.platform.worker.listener;

import com.dingky.gis.ai.platform.common.model.LayerTaskMessage;
import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import com.dingky.gis.ai.platform.common.util.PgUtil;
import com.dingky.gis.ai.platform.worker.service.GdalService;
import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GdalService gdalService;
    public FileTaskConsumer(KafkaTemplate<String, Object> kafkaTemplate, GdalService gdalService) {
        this.kafkaTemplate = kafkaTemplate;
        this.gdalService = gdalService;
    }
    /**
     * 监听 Kafka 主题
     */
    @KafkaListener(topics = "file-upload-topic",
            containerFactory = "factory")
    public void consume(FileTaskMessage msg){
        log.info("====== 监听到file-upload-topic任务 ======");
        log.info("收到任务：{}", msg);
        // 1. 处理任务：解压文件
        log.info("解压文件：{}", msg.getFilePath());
        String filePath = unzipIfNeeded(msg.getFilePath());
        // 获取所有图层名
        List<String> layerNames = gdalService.getLayerNames(filePath);
        // 2.创建任务：图层解析
        for (String layerName : layerNames){
            LayerTaskMessage layerTaskMessage = new LayerTaskMessage();
            // 创建任务id并作为后续表名
            String taskId = PgUtil.generateTableName(filePath, layerName);
            layerTaskMessage.setTaskId(taskId);
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
        // 1. 校验文件是否存在
        File sourceFile = new File(filePath);
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            log.error("文件不存在：{}", filePath);
            return filePath;
        }

        // 2. 判断是否是zip文件（忽略大小写）
        String fileName = sourceFile.getName().toLowerCase();
        if (!fileName.endsWith(".zip")) {
            log.info("非zip文件，无需解压：{}", filePath);
            return filePath;
        }

        // 3. 定义解压目标目录：和原文件在同一个目录下
        // 例如：/test/a.zip → 解压到 /test/a/
        String parentPath = sourceFile.getParent();
        String destDirName = fileName.substring(0, fileName.lastIndexOf(".zip"));
        File destDir = new File(parentPath, destDirName);

        // 4. 执行解压
        try (FileInputStream fis = new FileInputStream(sourceFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            // 创建目标目录
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            ZipEntry entry;
            // 遍历zip内所有文件/文件夹
            while ((entry = zis.getNextEntry()) != null) {
                File destFile = new File(destDir, entry.getName());

                // 是文件夹：创建目录
                if (entry.isDirectory()) {
                    destFile.mkdirs();
                    continue;
                }

                // 是文件：先创建父目录，再写入文件
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }

                // 写入文件
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }

            log.info("解压完成！路径：{}", destDir.getAbsolutePath());
            return getShpFilePaths(destDir.getAbsolutePath()).get(0);

        } catch (IOException e) {
            log.error("解压失败：{}", e.getMessage());
            return filePath;
        }
    }
    /**
     * 获取路径下的shp数据绝对路径
     */
    private List<String> getShpFilePaths(String path) {
        List<String> shpFilePaths = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
        }
        for (File f : Objects.requireNonNull(file.listFiles())) {
            if (f.isFile() && f.getName().endsWith(".shp")) {
                shpFilePaths.add(f.getAbsolutePath());
            }
        }
        return shpFilePaths;
    }
}
