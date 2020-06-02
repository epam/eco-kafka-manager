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
package com.epam.eco.kafkamanager.udmetrics.config.repo.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.cache.CacheListener;
import com.epam.eco.commons.kafka.cache.KafkaCache;
import com.epam.eco.commons.kafka.config.ConsumerConfigBuilder;
import com.epam.eco.commons.kafka.config.ProducerConfigBuilder;
import com.epam.eco.commons.kafka.serde.JsonDeserializer;
import com.epam.eco.commons.kafka.serde.JsonSerializer;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfig;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfigRepo;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfigSearchCriteria;

/**
 * @author Andrei_Tytsik
 */
public class KafkaUDMetricConfigRepo extends AbstractKeyValueRepo<String, UDMetricConfig, UDMetricConfigSearchCriteria> implements UDMetricConfigRepo, CacheListener<String, UDMetricConfig>, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaUDMetricConfigRepo.class);

    private static final String TOPIC = "__eco_udm_configs";

    @Autowired
    private KafkaManagerProperties kafkaManagerProperties;
    @Autowired
    private KafkaUDMetricConfigRepoProperties configRepoProperties;

    private final List<UpdateListener> updateListeners = new CopyOnWriteArrayList<>();

    private KafkaCache<String, UDMetricConfig> configCache;

    @PostConstruct
    private void init() {
        initConfigCache();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startConfigCache();

        LOGGER.info("Started");
    }

    @Override
    public int getPhase() {
        return 1;
    }

    @PreDestroy
    private void destroy() {
        destroyConfigCache();

        LOGGER.info("Destroyed");
    }

    private void initConfigCache() {
        configCache = KafkaCache.<String, UDMetricConfig>builder().
                bootstrapServers(kafkaManagerProperties.getBootstrapServers()).
                topicName(TOPIC).
                bootstrapTimeoutInMs(configRepoProperties.getBootstrapTimeoutInMs()).
                consumerConfigBuilder(ConsumerConfigBuilder.
                        with(kafkaManagerProperties.getClientConfig()).
                        keyDeserializerString().
                        valueDeserializer(JsonDeserializer.class).
                        property(JsonDeserializer.VALUE_TYPE, UDMetricConfig.class)).
                readOnly(false).
                producerConfigBuilder(ProducerConfigBuilder.
                        with(kafkaManagerProperties.getClientConfig()).
                        keySerializerString().
                        valueSerializer(JsonSerializer.class)).
                listener(this).
                build();
    }

    private void startConfigCache() throws Exception {
        configCache.start();
    }

    private void destroyConfigCache() {
        configCache.close();
    }

    @Override
    public int size() {
        return configCache.size();
    }

    @Override
    public boolean contains(String name) {
        Validate.notBlank(name, "UDM name is blank");

        return configCache.containsKey(name);
    }

    @Override
    public UDMetricConfig get(String name) {
        Validate.notBlank(name, "UDM name is blank");

        UDMetricConfig config = configCache.get(name);
        if (config == null) {
            throw new RuntimeException(
                    String.format("UDM config not found by name = %s", name));
        }

        return config;
    }

    @Override
    public List<UDMetricConfig> values() {
        return configCache.values();
    }

    @Override
    public List<UDMetricConfig> values(List<String> names) {
        Validate.notEmpty(names, "UDM name list is null or empty");
        Validate.noNullElements(names, "UDM name list has null elements");

        List<UDMetricConfig> configs = new ArrayList<>();
        names.forEach(name -> {
            configs.add(get(name));
        });
        return configs;
    }

    @Override
    public List<String> keys() {
        return configCache.keysAsList();
    }

    @Override
    public void createOrReplace(UDMetricConfig config) {
        Validate.notNull(config, "UDM config is null");

        configCache.put(config.getName(), config);
    }

    @Override
    public void remove(String name) {
        Validate.notBlank(name, "UDM name is blank");

        configCache.remove(name);
    }

    @Override
    public void addUpdateListener(UpdateListener listener)  {
        Validate.notNull(listener, "Listener is null");

        updateListeners.add(listener);
    }

    @Override
    public void onCacheUpdated(Map<String, UDMetricConfig> update) {
        updateListeners.forEach(listener -> {
            update.entrySet().forEach(e -> {
                String name = e.getKey();
                UDMetricConfig config = e.getValue();
                if (config != null) {
                    try {
                        listener.onConfigUpdated(name, config);
                    } catch (Exception ex) {
                        LOGGER.error(
                                String.format(
                                        "Failed to handle 'config updated' event. Name = %s, config = %s",
                                        name, config),
                                ex);
                    }
                } else {
                    try {
                        listener.onConfigRemoved(name);
                    } catch (Exception ex) {
                        LOGGER.error(
                                String.format(
                                        "Failed to handle 'config removed' event. Name = %s",
                                        name),
                                ex);
                    }
                }
            });
        });
    }

}
