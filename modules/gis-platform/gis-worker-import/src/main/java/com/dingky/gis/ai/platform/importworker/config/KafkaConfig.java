package com.dingky.gis.ai.platform.importworker.config;


import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaConfig
 * Package: com.dingky.gis.ai.platform.importworker.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/10 14:39
 * @Version 1.0
 **/
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {
    private final KafkaPropertiesExt kafkaPropertiesExt;
    public KafkaConfig(KafkaPropertiesExt kafkaPropertiesExt) {
        this.kafkaPropertiesExt = kafkaPropertiesExt;
    }
    /**
     * ======================
     * Consumer（消费 import-feature-topic）
     * ======================
     */
    @Bean
    public ConsumerFactory<String , Object> consumerFactory(){
        Map<String, Object> config = new HashMap<>();
        // Kafka地址（服务器）
        String bootstrapServers = "hostlocal:9092";
        if (kafkaPropertiesExt != null && kafkaPropertiesExt.getBootstrapServers() != null){
            bootstrapServers = kafkaPropertiesExt.getBootstrapServers();
        }
        config.put("bootstrap.servers", bootstrapServers);
        String groupId = "gis-import-group";
        if (kafkaPropertiesExt.getConsumer() != null && kafkaPropertiesExt.getConsumer().getGroupId() != null){
            groupId = kafkaPropertiesExt.getConsumer().getGroupId();
        }
        log.info("worker KafkaConfig 创建 Kafka 监听器：" + groupId);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // 允许反序列化你的类
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // 关键配置：防止消费者主动离开组
        /*
        SESSION_TIMEOUT_MS_CONFIG: 会话超时时间（30秒）
        HEARTBEAT_INTERVAL_MS_CONFIG: 心跳间隔（10秒）
        MAX_POLL_INTERVAL_MS_CONFIG: 最大轮询间隔（10分钟）
        MAX_POLL_RECORDS_CONFIG: 每次轮询最大记录数（100条）
        这些配置确保消费者有足够时间处理大批量数据
         */
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 并发数
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * ======================
     * Producer（发送 import-feature-topic）
     * ======================
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        // Kafka地址（服务器）
        String bootstrapServers = "hostlocal:9092";
        if (kafkaPropertiesExt != null && kafkaPropertiesExt.getBootstrapServers() != null){
            bootstrapServers = kafkaPropertiesExt.getBootstrapServers();
        }
        // "bootstrap.servers"
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // key序列化
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // value序列化（对象 → JSON）
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }


}
