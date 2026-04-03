package com.dingky.gis.ai.platform.worker.config;

import com.dingky.gis.ai.platform.common.model.KafkaPropertiesExt;
import com.dingky.gis.ai.platform.common.model.FileTaskMessage;
import com.dingky.gis.ai.platform.common.model.LayerTaskMessage;
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
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

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
@Slf4j
public class KafkaConfig {
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
    private final KafkaPropertiesExt kafkaPropertiesExt;
    public KafkaConfig(KafkaPropertiesExt kafkaPropertiesExt) {
        this.kafkaPropertiesExt = kafkaPropertiesExt;
    }

    /**
     * ======================
     * 2️⃣ Consumer（消费 file-task）
     * ======================
     */
    /**
     * 用于消费者监听器的配置
     * 创建 Kafka 监听器
     * @return
     */
    @Bean
    public ConsumerFactory<String, FileTaskMessage> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesExt.getBootstrapServers());
        String groupId = "shp-parsing-group";
        if (kafkaPropertiesExt.getConsumer() != null && kafkaPropertiesExt.getConsumer().getGroupId() != null){
            groupId = kafkaPropertiesExt.getConsumer().getGroupId();
        }
        log.info("worker KafkaConfig 创建 Kafka 监听器：" + groupId);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 允许反序列化你的类
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(FileTaskMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FileTaskMessage> factory() {
        ConcurrentKafkaListenerContainerFactory<String, FileTaskMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }


    /**
     * ======================
     * 1️⃣ Producer（发送 layer-task）
     * ======================
     */
    /**
     * Kafka生产者工厂
     */
    @Bean
    public ProducerFactory<String , LayerTaskMessage> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        // Kafka地址（你的服务器）
        if (kafkaPropertiesExt != null && kafkaPropertiesExt.getBootstrapServers() != null){
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesExt.getBootstrapServers());
        }else {
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
     * 创建 Kafka 发送器（Producer）
     */
    @Bean
    public KafkaTemplate<String, LayerTaskMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
