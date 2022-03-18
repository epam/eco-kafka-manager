/*******************************************************************************
 *  Copyright 2022 EPAM Systems
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
package com.epam.eco.kafkamanager.core.metadata.repo.kafka;

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
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataSearchCriteria;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;

/**
 * @author Andrei_Tytsik
 */
public class KafkaMetadataRepo extends AbstractKeyValueRepo<MetadataKey, Metadata, MetadataSearchCriteria> implements MetadataRepo, CacheListener<MetadataKey, Metadata>, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaMetadataRepo.class);

    private static final String TOPIC = "__eco_entity_metadata";

    @Autowired
    private KafkaManagerProperties properties;

    private final List<MetadataUpdateListener> updateListeners = new CopyOnWriteArrayList<>();

    private KafkaCache<MetadataKey, Metadata> metadataCache;

    @PostConstruct
    private void init() {
        initMetadataCache();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startMetadataCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() {
        destroyMetadataCache();

        LOGGER.info("Destroyed");
    }

    private void initMetadataCache() {
        metadataCache = KafkaCache.<MetadataKey, Metadata>builder().
                bootstrapServers(properties.getBootstrapServers()).
                topicName(TOPIC).
                bootstrapTimeoutInMs(properties.getMetadataStoreBootstrapTimeoutInMs()).
                consumerConfigBuilder(ConsumerConfigBuilder.
                        with(properties.getClientConfig()).
                        keyDeserializer(JsonDeserializer.class).
                        property(JsonDeserializer.KEY_TYPE, MetadataKey.class).
                        valueDeserializer(JsonDeserializer.class).
                        property(JsonDeserializer.VALUE_TYPE, Metadata.class)).
                readOnly(false).
                producerConfigBuilder(ProducerConfigBuilder.
                        with(properties.getClientConfig()).
                        keySerializer(JsonSerializer.class).
                        valueSerializer(JsonSerializer.class)).
                listener(this).
                build();
    }

    private void startMetadataCache() throws Exception {
        metadataCache.start();
    }

    private void destroyMetadataCache() {
        metadataCache.close();
    }

    @Override
    public int size() {
        return metadataCache.size();
    }

    @Override
    public boolean contains(MetadataKey key) {
        Validate.notNull(key, "Metadata key can't be null");

        return metadataCache.containsKey(key);
    }

    @Override
    public Metadata get(MetadataKey key) {
        Validate.notNull(key, "Metadata key can't be null");

        return metadataCache.get(key);
    }

    @Override
    public List<Metadata> values() {
        return metadataCache.values();
    }

    @Override
    public List<Metadata> values(List<MetadataKey> keys) {
        Validate.notEmpty(keys, "Keys list can't be null or empty");
        Validate.noNullElements(keys, "Keys list can't contains null elements");

        List<Metadata> metadata = new ArrayList<>();
        keys.forEach(key -> metadata.add(get(key)));
        return metadata;
    }

    @Override
    public List<MetadataKey> keys() {
        return metadataCache.keysAsList();
    }

    @Override
    public void createOrReplace(MetadataKey key, Metadata metadata) {
        Validate.notNull(key, "Key can't be null");
        Validate.notNull(metadata, "Metadata can't be null");

        metadata = metadata.toBuilder().updatedAtNow().build();
        metadataCache.put(key, metadata);
    }

    @Override
    public void delete(MetadataKey key) {
        Validate.notNull(key, "Key can't be null");

        metadataCache.remove(key);
    }

    @Override
    public void registerUpdateListener(MetadataUpdateListener listener)  {
        Validate.notNull(listener, "Listener can't be null");

        updateListeners.add(listener);
    }

    @Override
    public void onCacheUpdated(Map<MetadataKey, Metadata> update) {
        Validate.notNull(update, "Update can't be null");

        updateListeners.forEach(listener -> update.forEach((key, metadata) -> {
            if (metadata != null) {
                try {
                    listener.onMetadataUpdated(key, metadata);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'metadata updated' event. Key = %s, metadata = %s",
                                    key, metadata),
                            ex);
                }
            } else {
                try {
                    listener.onMetadataRemoved(key);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'metadata removed' event. Key = %s",
                                    key),
                            ex);
                }
            }
        }));
    }

}
