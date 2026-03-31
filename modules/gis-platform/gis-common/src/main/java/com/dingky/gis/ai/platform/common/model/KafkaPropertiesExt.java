package com.dingky.gis.ai.platform.common.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaPropertiesExt
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 16:21
 * @Version 1.0
 **/

/** Kafka 的“配置模型层”
 * Kafka 配置模型（企业级标准写法）
 * 作用：将 application.yml 中的配置映射为 Java 对象
 */
@Data
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaPropertiesExt {

    /**
     * Kafka 集群地址
     */
    private String bootstrapServers;

    /**
     * 消费者配置
     * Spring 是根据“配置结构”去构造对象树，而不是根据类名
     */
    private Consumer consumer = new Consumer();

    /**
     * 生产者配置（预留扩展）
     */
    private Producer producer = new Producer();

    @Data
    public static class Consumer {
        /**
         * 消费组ID
         * 未来可以扩展内容，比如：根据业务进行分组
         */
        private String groupId;
    }

    @Data
    public static class Producer {
        // 后续可以扩展 ack / 重试 等配置
    }
}
