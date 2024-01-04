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
package com.epam.eco.kafkamanager.ui.config.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epam.eco.commons.kafka.serde.JsonSerializer;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

/**
 * @author Mikhail_Vershkov
 */

@Configuration
@EnableConfigurationProperties(KafkaManagerUiProperties.class)
public class KmKafkaProducerConfiguration {

    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

    @Bean
    public KafkaProducerResolver kafkaProducerResolver(@Autowired List<KafkaKmUiProducer<?, ?>> producers,
                                                       KafkaManagerProperties properties) {
        return new KafkaProducerResolver(producers, kafkaStringKeyStringValueProducer(properties));
    }
    @Bean
    public KafkaKmUiProducer<String, String> kafkaStringKeyStringValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.STRING,
                kafkaStringKeyStringValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<String,Object> kafkaStringKeyAvroValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.AVRO,
                kafkaStringKeyAvroValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<Object,String> kafkaAvroKeyStringValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.AVRO,
                TopicRecordFetchParams.DataFormat.STRING,
                kafkaAvroKeyStringValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<Object,Object> kafkaAvroKeyAvroValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.AVRO,
                TopicRecordFetchParams.DataFormat.AVRO,
                kafkaAvroKeyAvroValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<String,Object> kafkaStringKeyJsonValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.JSON_STRING,
                kafkaStringKeyJsonValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<Object,String> kafkaJsonKeyStringValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.JSON_STRING,
                TopicRecordFetchParams.DataFormat.STRING,
                kafkaJsonKeyStringValueTemplate(properties));
    }
    @Bean
    public KafkaKmUiProducer<Object,Object> kafkaJsonKeyJsonValueProducer(KafkaManagerProperties properties) {
        return new KafkaKmUiProducer<>(
                TopicRecordFetchParams.DataFormat.JSON_STRING,
                TopicRecordFetchParams.DataFormat.JSON_STRING,
                kafkaJsonKeyJsonValueTemplate(properties));
    }


    @Bean
    public static KafkaTemplate<String, String> kafkaStringKeyStringValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, StringSerializer.class, StringSerializer.class));
    }
    @Bean
    public static KafkaTemplate<String, Object> kafkaStringKeyAvroValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, StringSerializer.class, KafkaAvroSerializer.class));
    }
    @Bean
    public static KafkaTemplate<Object, String> kafkaAvroKeyStringValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties,  KafkaAvroSerializer.class, StringSerializer.class));
    }
    @Bean
    public static KafkaTemplate<Object, Object> kafkaAvroKeyAvroValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, KafkaAvroSerializer.class, KafkaAvroSerializer.class));
    }
    @Bean
    public static KafkaTemplate<String, Object> kafkaStringKeyJsonValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, StringSerializer.class, JsonSerializer.class));
    }
    @Bean
    public static KafkaTemplate<Object, String> kafkaJsonKeyStringValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, JsonSerializer.class, StringSerializer.class));
    }
    @Bean
    public static KafkaTemplate<Object, Object> kafkaJsonKeyJsonValueTemplate(KafkaManagerProperties properties) {
        return new KafkaTemplate<>(getProducerFactory(properties, JsonSerializer.class, JsonSerializer.class));
    }

    private static <K,V> ProducerFactory<K,V> getProducerFactory(KafkaManagerProperties properties,
                                                          Class<? extends Serializer<K>> keySerializer,
                                                          Class<? extends Serializer<V>> valueSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        configProps.put(SCHEMA_REGISTRY_URL,properties.getSchemaRegistryUrl());
        if(MapUtils.isNotEmpty(properties.getClientConfig())) {
            configProps.putAll(properties.getClientConfig());
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}
