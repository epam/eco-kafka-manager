/*******************************************************************************
 *  Copyright 2023 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 *******************************************************************************/
package com.epam.eco.kafkamanager.ui.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.epam.eco.kafkamanager.KafkaTombstoneProducer;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

/**
 * @author Mikhail_Vershkov
 */

@Configuration
@EnableConfigurationProperties(KafkaManagerUiProperties.class)
public class TombstoneProducerConfiguration {

    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

    @Bean
    public KafkaTombstoneProducer<String,Object> kafkaTombstoneStringProducer(KafkaManagerProperties properties) {
        return new KafkaTombstoneProducer<>(kafkaStringTemplate(properties));
    }
    @Bean
    public KafkaTombstoneProducer<Object,Object> kafkaTombstoneAvroProducer(KafkaManagerProperties properties) {
        return new KafkaTombstoneProducer<>(kafkaAvroTemplate(properties));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaStringTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(tombStoneStringProducerFactory(properties));
    }
    @Bean
    public KafkaTemplate<Object, Object> kafkaAvroTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(tombStoneAvroProducerFactory(properties));
    }

    @Bean
    public ProducerFactory<String, Object> tombStoneStringProducerFactory(KafkaManagerProperties properties) {
        return getProducerFactory(properties, StringSerializer.class);
    }
    @Bean
    public ProducerFactory<Object, Object> tombStoneAvroProducerFactory(KafkaManagerProperties properties) {
        return getProducerFactory(properties, KafkaAvroSerializer.class);
    }

    private <T,V> ProducerFactory<T,V> getProducerFactory(KafkaManagerProperties properties,
                                                          Class<? extends Serializer<?>> serializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
        configProps.put(SCHEMA_REGISTRY_URL,properties.getSchemaRegistryUrl());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}
