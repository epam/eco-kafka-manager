/*******************************************************************************
 *  Copyright 2024 EPAM Systems
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

import com.epam.eco.commons.kafka.serde.JsonSerializer;
import com.epam.eco.kafkamanager.KafkaKmProducer;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.ui.config.producer.KafkaKmUiProducer;
import com.epam.eco.kafkamanager.ui.config.producer.KafkaProducerResolver;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaAvroKeyAvroValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaAvroKeyStringValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaJsonKeyJsonValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaJsonKeyStringValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaStringKeyAvroValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaStringKeyJsonValueTemplate;
import static com.epam.eco.kafkamanager.ui.config.producer.KmKafkaProducerConfiguration.kafkaStringKeyStringValueTemplate;

/**
 * @author Mikhail_Vershkov
 */
public class KafkaProducerResolverTest {

    private static final String BOOTSTRAP_SERVER = "test.kafka.server";
    private static final String SCHEMA_REGISTRY_URL = "schema.registry.utl";
    private static final String KEY_SERIALIZER_CONFIG = "key.serializer";
    private static final String VALUE_SERIALIZER_CONFIG = "value.serializer";
    private static KafkaProducerResolver kafkaProducerResolver;

    @BeforeAll
    public static void beforeAll() {
        KafkaManagerProperties properties = new KafkaManagerProperties();
        properties.setBootstrapServers(BOOTSTRAP_SERVER);
        properties.setSchemaRegistryUrl(SCHEMA_REGISTRY_URL);
        List<KafkaKmUiProducer<?, ?>> producers = List.of(
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.STRING,
                                        TopicRecordFetchParams.DataFormat.STRING,
                                        kafkaStringKeyStringValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.STRING,
                                        TopicRecordFetchParams.DataFormat.AVRO,
                                        kafkaStringKeyAvroValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.AVRO,
                                        TopicRecordFetchParams.DataFormat.STRING,
                                        kafkaAvroKeyStringValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.AVRO,
                                        TopicRecordFetchParams.DataFormat.AVRO,
                                        kafkaAvroKeyAvroValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.STRING,
                                        TopicRecordFetchParams.DataFormat.JSON_STRING,
                                        kafkaStringKeyJsonValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.JSON_STRING,
                                        TopicRecordFetchParams.DataFormat.STRING,
                                        kafkaJsonKeyStringValueTemplate(properties)),
                new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.JSON_STRING,
                                        TopicRecordFetchParams.DataFormat.JSON_STRING,
                                        kafkaJsonKeyJsonValueTemplate(properties)));
        kafkaProducerResolver = new KafkaProducerResolver(producers,new KafkaKmUiProducer<>(TopicRecordFetchParams.DataFormat.STRING,
                                        TopicRecordFetchParams.DataFormat.STRING,
                                        kafkaStringKeyStringValueTemplate(properties)));
    }

    @Test
    void testResolverStringKeyStringValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.STRING, TopicRecordFetchParams.DataFormat.STRING));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(StringSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(StringSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }
    @Test
    void testResolverStringKeyAvroValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.STRING, TopicRecordFetchParams.DataFormat.AVRO));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(StringSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(KafkaAvroSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }

    @Test
    void testResolverAvroKeyStringValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.AVRO, TopicRecordFetchParams.DataFormat.STRING));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(KafkaAvroSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(StringSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }

    @Test
    void testResolverAvroKeyAvroValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.AVRO, TopicRecordFetchParams.DataFormat.AVRO));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(KafkaAvroSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(KafkaAvroSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }
    @Test
    void testResolverStringKeyJsonValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.STRING, TopicRecordFetchParams.DataFormat.JSON_STRING));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(StringSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(JsonSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }
    @Test
    void testResolverJsonKeyStringValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.JSON_STRING, TopicRecordFetchParams.DataFormat.STRING));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(JsonSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(StringSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }
    @Test
    void testResolverJsonKeyJsonValue() {
        KafkaKmProducer<?,?> kafkaKmProducer = kafkaProducerResolver.resolve(
                new KafkaKmUiProducer.KafkaProducerType(TopicRecordFetchParams.DataFormat.JSON_STRING, TopicRecordFetchParams.DataFormat.JSON_STRING));
        Map<String, Object> config =  kafkaKmProducer.getKafkaTemplate().getProducerFactory().getConfigurationProperties();
        Assertions.assertEquals(JsonSerializer.class, config.get(KEY_SERIALIZER_CONFIG));
        Assertions.assertEquals(JsonSerializer.class, config.get(VALUE_SERIALIZER_CONFIG));
    }
}
