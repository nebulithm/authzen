package org.authzen.examples.webflux.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.authzen.examples.webflux.kafka.PrincipalPolicyKafkaMessage;
import org.authzen.examples.webflux.kafka.ResourceKafkaMessage;
import org.authzen.examples.webflux.kafka.RolePolicyKafkaMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResourceKafkaMessage> resourceKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        return buildFactory(kafkaProperties, ResourceKafkaMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PrincipalPolicyKafkaMessage> principalPolicyKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        return buildFactory(kafkaProperties, PrincipalPolicyKafkaMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RolePolicyKafkaMessage> rolePolicyKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        return buildFactory(kafkaProperties, RolePolicyKafkaMessage.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildFactory(KafkaProperties kafkaProperties, Class<T> type) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, type.getName());

        ConsumerFactory<String, T> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
