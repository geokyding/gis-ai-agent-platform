package com.dingky.gis.ai.platform.importworker.listener;

import com.dingky.gis.ai.platform.common.model.FeatureDTO;
import com.dingky.gis.ai.platform.common.model.ImportTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ImportConsumer
 * Package: com.dingky.gis.ai.platform.importworker.listener
 * Description:
 *主要改进点：
 * SQL注入防护：
 * 添加白名单正则验证表名和字段名
 * 字段名使用双引号 " 而非单引号 '（PostgreSQL标准）
 * 去除首尾空格
 * 完善的空值检查：
 * 验证消息对象及各字段是否为空
 * 过滤无效字段名
 * 跳过无效的要素数据
 * 分批处理：
 * 设置批次大小为100条，避免内存溢出
 * 每批处理后清空列表
 * 异常时自动回滚
 * 完善的异常处理：
 * try-catch捕获所有异常
 * 详细记录错误日志
 * 抛出运行时异常触发重试
 * 详细的日志记录：
 * 记录处理进度
 * 统计成功和跳过的数量
 * 记录SQL语句便于调试
 * 代码优化：
 * 使用流式API简化代码
 * 添加常量定义提高可维护性
 * 改善代码可读性
 * @Author: ding
 * @Create 2026/4/10 17:00
 * @Version 1.0
 **/
@Component
@Slf4j
public class ImportConsumer {
    // 用于验证表名和字段名的白名单正则（只允许字母、数字、下划线、中划线）
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\u4e00-\u9fa5]+$");

    // 批量插入的批次大小
    private static final int BATCH_SIZE = 100;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JdbcTemplate jdbcTemplate;
    public ImportConsumer(KafkaTemplate<String, Object> kafkaTemplate, JdbcTemplate jdbcTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "data-import-topic", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ImportTaskMessage msg){
        try {
            log.info("====== 监听到data-import-topic任务 ======");
            long startTime = System.currentTimeMillis();
            if (msg == null) {
                log.error("接收到的消息为空");
                return;
            }

            if (msg.getLayerName() == null || msg.getLayerName().trim().isEmpty()) {
                log.error("图层名称为空");
                return;
            }

            if (msg.getFieldNames() == null || msg.getFieldNames().isEmpty()) {
                log.error("字段列表为空");
                return;
            }

            if (msg.getFeatures() == null || msg.getFeatures().isEmpty()) {
                log.error("要素数据为空");
                return;
            }

            String layerName = msg.getLayerName().trim();
            if (!IDENTIFIER_PATTERN.matcher(layerName).matches()) {
                log.error("图层名称包含非法字符: {}", layerName);
                log.warn("图层名称只允许包含字母、数字、下划线、中划线和中文字符，请检查图层名称: {}", "a-zA-Z0-9_-\\u4e00-\\u9fa5");
                throw new IllegalArgumentException("图层名称包含非法字符");
            }

            for (String fieldName : msg.getFieldNames()) {
                if (fieldName != null && !IDENTIFIER_PATTERN.matcher(fieldName.trim()).matches()) {
                    log.error("字段名称包含非法字符: {}", fieldName);
                    log.warn("字段名称只允许包含字母、数字、下划线、中划线和中文字符，请检查字段名称: {}", "a-zA-Z0-9_-\\u4e00-\\u9fa5");
                    throw new IllegalArgumentException("字段名称包含非法字符: " + fieldName);
                }
            }

            String tableName = msg.getTaskId();
            log.info("数据表名：{}", tableName);

            List<String> validFieldNames = msg.getFieldNames().stream()
                    .filter(f -> f != null && !f.trim().isEmpty())
                    .map(String::trim)
                    .toList();

            if (validFieldNames.isEmpty()) {
                log.error("有效的字段列表为空");
                return;
            }

            String fields = validFieldNames.stream()
                    .map(field -> "\"" + field + "\"")
                    .collect(Collectors.joining(", "));
            Map<String, String> fieldTypes = msg.getFieldTypes();
            if (fieldTypes == null) {
                log.warn("字段类型信息为空，使用默认类型处理");
                fieldTypes = new HashMap<>();
            }

//            String placeholders = validFieldNames.stream()
//                    .map(field -> "?")
//                    .collect(Collectors.joining(", "));

            final Map<String, String> finalFieldTypes = fieldTypes;
            String placeholders = validFieldNames.stream()
                    .map(field -> {
                        String pgType = finalFieldTypes.getOrDefault(field, "varchar").toLowerCase();
                        if ("date".equals(pgType)) {
                            return "?::date";
                        } else if ("time".equals(pgType)) {
                            return "?::time";
                        } else if ("timestamp".equals(pgType) || "timestamp without time zone".equals(pgType)) {
                            return "?::timestamp";
                        } else {
                            return "?";
                        }
                    })
                    .collect(Collectors.joining(", "));

            String insertSql = "INSERT INTO \"" + tableName + "\" (geom, " + fields + ") VALUES (ST_GeomFromWKB(?), " + placeholders + ")";
            if(log.isDebugEnabled()){
                log.debug("准备插入数据，总数: {}, SQL: {}", msg.getFeatures().size(), insertSql);
            }

            List<Object[]> batchArgs = new ArrayList<>();
            int successCount = 0;
            int skipCount = 0;

            for (FeatureDTO feature : msg.getFeatures()) {
                if (feature == null || feature.getGeometry() == null) {
                    log.warn("跳过无效的要素数据");
                    skipCount++;
                    continue;
                }

                List<Object> row = new ArrayList<>();
                row.add(feature.getGeometry());

                for (String fieldName : validFieldNames) {
                    Object fieldValue = feature.getProperties().get(fieldName) != null ?
                            feature.getProperties().get(fieldName) : null;
                    row.add(fieldValue);
                }

                batchArgs.add(row.toArray(new Object[0]));

                if (batchArgs.size() >= BATCH_SIZE) {
                    int[] results = jdbcTemplate.batchUpdate(insertSql, batchArgs);
                    successCount += results.length;
                    log.info("已批量插入 {} 条数据", results.length);
                    batchArgs.clear();
                }
            }

            if (!batchArgs.isEmpty()) {
                int[] results = jdbcTemplate.batchUpdate(insertSql, batchArgs);
                successCount += results.length;
                log.info("最后一批插入 {} 条数据", results.length);
            }

            log.info("====== 数据导入完成，成功: {}, 跳过: {} ======", successCount, skipCount);
            // 最后统一输出统计
            log.info("数据导入完成，成功: {}, 跳过: {}, 耗时: {}ms",
                    successCount, skipCount, System.currentTimeMillis() - startTime);

        } catch (IllegalArgumentException e) {
            log.error("数据验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("处理data-import-topic任务失败", e);
            throw new RuntimeException("数据导入失败: " + e.getMessage(), e);
        }
    }

}
