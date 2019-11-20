/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.core.autoconfigure;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.epam.eco.commons.kafka.config.AdminClientConfigBuilder;
import com.epam.eco.commons.kafka.config.ConsumerConfigBuilder;
import com.epam.eco.commons.kafka.config.ProducerConfigBuilder;
import com.epam.eco.kafkamanager.DatePeriod;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

/**
 * @author Andrei_Tytsik
 */
@ConfigurationProperties(prefix = "eco.kafkamanager.core")
public class KafkaManagerProperties {

    public static final String GROUP_ID = "_eco_kafka_manager";

    private String bootstrapServers;
    private String schemaRegistryUrl;
    private Map<String, Object> clientConfig;
    private long metadataStoreBootstrapTimeoutInMs = 3 * 60 * 1000;
    private long transactionStoreBootstrapTimeoutInMs = 3 * 60 * 1000;
    private DatePeriod transactionStoreBootstrapDataFreshness = DatePeriod.ONE_HOUR;

    private Map<String, Object> commonConsumerConfig;
    private Map<String, Object> commonProducerConfig;
    private Map<String, Object> commonAdminClientConfig;

    @PostConstruct
    private void init() {
        commonConsumerConfig = buildCommonConsumerConfig(null);
        commonProducerConfig = buildCommonProducerConfig(null);
        commonAdminClientConfig = buildCommonAdminClientConfig(null);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }
    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }
    public Map<String, Object> getClientConfig() {
        return clientConfig;
    }
    public void setClientConfig(Map<String, Object> clientConfig) {
        this.clientConfig = clientConfig;
    }
    public long getMetadataStoreBootstrapTimeoutInMs() {
        return metadataStoreBootstrapTimeoutInMs;
    }
    public void setMetadataStoreBootstrapTimeoutInMs(long metadataStoreBootstrapTimeoutInMs) {
        this.metadataStoreBootstrapTimeoutInMs = metadataStoreBootstrapTimeoutInMs;
    }
    public long getTransactionStoreBootstrapTimeoutInMs() {
        return transactionStoreBootstrapTimeoutInMs;
    }
    public void setTransactionStoreBootstrapTimeoutInMs(long transactionStoreBootstrapTimeoutInMs) {
        this.transactionStoreBootstrapTimeoutInMs = transactionStoreBootstrapTimeoutInMs;
    }
    public DatePeriod getTransactionStoreBootstrapDataFreshness() {
        return transactionStoreBootstrapDataFreshness;
    }
    public void setTransactionStoreBootstrapDataFreshness(DatePeriod transactionStoreBootstrapDataFreshness) {
        this.transactionStoreBootstrapDataFreshness = transactionStoreBootstrapDataFreshness;
    }
    public Map<String, Object> getCommonConsumerConfig() {
        return commonConsumerConfig;
    }
    public void setCommonConsumerConfig(Map<String, Object> commonConsumerConfig) {
        this.commonConsumerConfig = commonConsumerConfig;
    }
    public Map<String, Object> getCommonProducerConfig() {
        return commonProducerConfig;
    }
    public void setCommonProducerConfig(Map<String, Object> commonProducerConfig) {
        this.commonProducerConfig = commonProducerConfig;
    }
    public Map<String, Object> getCommonAdminClientConfig() {
        return commonAdminClientConfig;
    }
    public void setCommonAdminClientConfig(Map<String, Object> commonAdminClientConfig) {
        this.commonAdminClientConfig = commonAdminClientConfig;
    }

    public Map<String, Object> buildCommonConsumerConfig(
            Consumer<ConsumerConfigBuilder> enricher) {
        ConsumerConfigBuilder builder = ConsumerConfigBuilder.with(getClientConfig()).
                bootstrapServers(bootstrapServers).
                groupId(GROUP_ID);
        if (!StringUtils.isBlank(schemaRegistryUrl)) {
            builder.property(
                    KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                    schemaRegistryUrl);
        }
        if (enricher != null) {
            enricher.accept(builder);
        }
        return builder.build();
    }

    public Map<String, Object> buildCommonProducerConfig(
            Consumer<ProducerConfigBuilder> enricher) {
        ProducerConfigBuilder builder = ProducerConfigBuilder.with(getClientConfig()).
                bootstrapServers(bootstrapServers);
        if (!StringUtils.isBlank(schemaRegistryUrl)) {
            builder.property(
                    KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                    schemaRegistryUrl);
        }
        if (enricher != null) {
            enricher.accept(builder);
        }
        return builder.build();
    }

    public Map<String, Object> buildCommonAdminClientConfig(
            Consumer<AdminClientConfigBuilder> enricher) {
        AdminClientConfigBuilder builder = AdminClientConfigBuilder.with(getClientConfig()).
                bootstrapServers(bootstrapServers);
        if (enricher != null) {
            enricher.accept(builder);
        }
        return builder.build();
    }

}
