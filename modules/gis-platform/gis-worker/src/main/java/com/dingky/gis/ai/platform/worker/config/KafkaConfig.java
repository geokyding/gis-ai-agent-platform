package com.dingky.gis.ai.platform.worker.config;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import com.dingky.gis.ai.platform.common.model.TaskMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: KafkaConfig
 * Package: com.dingky.gis.ai.platform.worker.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 10:38
 * @Version 1.0
 **/

/** Kafka 的“运行时构建层”
 * 创建 Kafka 消费器（Consumer） 收消息的人
 * 创建ConcurrentKafkaListenerContainerFactory bean
 * Kafka
 *    ↓
 * @KafkaListener
 *    ↓
 * ShpConsumer
 */
@Configuration
@EnableKafka
public class KafkaConfig {
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
    private final KafkaPropertiesExt kafkaPropertiesExt;
    public KafkaConfig(KafkaPropertiesExt kafkaPropertiesExt) {
        this.kafkaPropertiesExt = kafkaPropertiesExt;
    }

    @Bean
    public ConsumerFactory<String, TaskMessage> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesExt.getBootstrapServers());
        String groupId = "shp-parsing-group";
        if (kafkaPropertiesExt.getConsumer() != null && kafkaPropertiesExt.getConsumer().getGroupId() != null){
            groupId = kafkaPropertiesExt.getConsumer().getGroupId();
        }
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 允许反序列化你的类
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(TaskMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskMessage> factory() {
        ConcurrentKafkaListenerContainerFactory<String, TaskMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
