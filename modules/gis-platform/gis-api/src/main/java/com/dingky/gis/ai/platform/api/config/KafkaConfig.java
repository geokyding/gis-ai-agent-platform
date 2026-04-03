package com.dingky.gis.ai.platform.api.config;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaConfig
 * Package: com.dingky.gis.ai.platform.api.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 10:22
 * @Version 1.0
 **/

/**
 * 创建 Kafka 发送器（Producer）
 * 1. 创建 KafkaTemplate
 * UploadController
 *    ↓
 * KafkaTemplate.send()
 *    ↓
 * Kafka
 */
@Configuration
@Slf4j
public class KafkaConfig {

    private final KafkaPropertiesExt kafkaPropertiesExt;
    public KafkaConfig(KafkaPropertiesExt kafkaPropertiesExt) {
        this.kafkaPropertiesExt = kafkaPropertiesExt;
    }

    /**
     * Kafka生产者工厂
     */
    @Bean
    public ProducerFactory<String , FileTaskMessage> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        // Kafka地址（你的服务器）
        if (kafkaPropertiesExt != null && kafkaPropertiesExt.getBootstrapServers() != null){
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesExt.getBootstrapServers());
        }else{
            log.info("未配置Kafka地址，使用默认地址：hostlocal:9092");
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hostlocal:9092");
        }
        // key序列化
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // value序列化（对象 → JSON）
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }
    /**
     * Kafka发送模板
     */
    @Bean
    public KafkaTemplate<String , FileTaskMessage> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 初始化
     * Spring 在创建完当前对象、注入完所有依赖之后，会自动调用加了 @PostConstruct 的方法。
     */
    @PostConstruct
    public void init(){
        log.info("KafkaConfig 初始化完成");
    }

}
