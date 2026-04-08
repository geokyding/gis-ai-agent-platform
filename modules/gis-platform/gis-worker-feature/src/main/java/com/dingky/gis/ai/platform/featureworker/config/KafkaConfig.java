package com.dingky.gis.ai.platform.featureworker.config;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaConfig
 * Package: com.dingky.gis.ai.platform.featureworker.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 16:06
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
     * Consumer（消费 feature-task）
     * ======================
     */
    @Bean
    public ConsumerFactory<String , Object> consumerFactory(){
        Map<String, Object> config = new HashMap<>();
        // Kafka地址（服务器）
        if (kafkaPropertiesExt != null && kafkaPropertiesExt.getBootstrapServers() != null){
            config.put("bootstrap.servers", kafkaPropertiesExt.getBootstrapServers());
        }else {
            log.info("未配置Kafka地址，使用默认地址：hostlocal:9092");
            config.put("bootstrap.servers", "hostlocal:9092");
        }
        String groupId = "gis-feature-group";
        if (kafkaPropertiesExt.getConsumer() != null && kafkaPropertiesExt.getConsumer().getGroupId() != null){
            groupId = kafkaPropertiesExt.getConsumer().getGroupId();
        }
        log.info("worker KafkaConfig 创建 Kafka 监听器：" + groupId);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 允许反序列化你的类
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
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

}
