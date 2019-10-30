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
package com.epam.eco.kafkamanager.core.topic.exec;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.helpers.TopicPurger;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class TopicPurgerTaskExecutorImpl extends AbstractAsyncStatefullTaskExecutor<String, Void> implements TopicPurgerTaskExecutor {

    private static final String ATTR_BACKUP_CONFIG = "__topic_purger_backup_config";

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    public TopicPurgerTaskExecutorImpl(CacheManager cacheManager) {
        super(cacheManager);
    }

    public TopicPurgerTaskExecutorImpl(ExecutorService executor, CacheManager cacheManager) {
        super(executor, cacheManager);
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    @Override
    protected TaskResult<Void> doExecute(String topicName) {
        return TaskResult.of(() -> internalExecute(topicName));
    }

    private Void internalExecute(String topicName) {
        kafkaManager.getTopic(topicName); // sanity check just for case topic doesn't exist

        TopicPurger topicPurger = TopicPurger.with(properties.getCommonConsumerConfig());

        Map<String, String> backupConfigOld = getBackupConfig(topicName);
        if (backupConfigOld != null) {
            topicPurger.restore(topicName, backupConfigOld);
        } else {
            topicPurger.purge(topicName, backupConfigNew -> updateBackupConfig(topicName, backupConfigNew));
        }
        deleteBackupConfig(topicName);

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getBackupConfig(String topicName) {
        try {
            return kafkaManager.getTopic(topicName).getMetadata().
                    map(metadata -> metadata.getAttribute(ATTR_BACKUP_CONFIG, Map.class)).
                    orElse(null);
        } catch (Exception ex) {
            return null; // any unexpected data is considered as "no backup"
        }
    }

    private void deleteBackupConfig(String topicName) {
        updateBackupConfig(topicName, null);
    }

    private void updateBackupConfig(String topicName, Map<String, String> config) {
        Metadata metadata = kafkaManager.getTopic(topicName).getMetadata().orElse(null);
        if (metadata != null && Objects.equals(metadata.getAttribute(ATTR_BACKUP_CONFIG), config)) {
            return;
        }

        TopicMetadataUpdateParams.Builder builder = TopicMetadataUpdateParams.builder(metadata).
                topicName(topicName);
        if (config == null) {
            builder.removeAttribute(ATTR_BACKUP_CONFIG);
        } else {
            builder.appendAttribute(ATTR_BACKUP_CONFIG, config);
        }

        kafkaManager.updateTopic(builder.build());
    }

}
