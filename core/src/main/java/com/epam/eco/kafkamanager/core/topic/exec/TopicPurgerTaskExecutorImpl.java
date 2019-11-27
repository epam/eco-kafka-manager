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

import java.util.Collections;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.CleanupPolicy;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class TopicPurgerTaskExecutorImpl extends AbstractTaskExecutor<String, Void, Void> implements TopicPurgerTaskExecutor {

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    protected KafkaManagerProperties properties;

    @Override
    protected TaskResult<Void> doExecute(String topicName, Void input) {
        return TaskResult.of(() -> internalExecute(topicName));
    }

    private Void internalExecute(String topicName) {
        String cleanupPolicyOrig = getCleanupPolicy(topicName);
        try {
            changePolicyForDeleteIfNeeded(topicName, cleanupPolicyOrig);
            adminOperations.deleteAllRecords(topicName);
        } finally {
            restorePolicyAfterDeleteIfNeeded(topicName, cleanupPolicyOrig);
        }
        return null;
    }

    private String getCleanupPolicy(String topicName) {
        Config config = adminOperations.describeTopicConfig(topicName);
        ConfigEntry entry = config.get(TopicConfig.CLEANUP_POLICY_CONFIG);
        return entry.value();
    }

    private void changePolicyForDeleteIfNeeded(String topicName, String cleanupPolicyOrig) {
        if (!CleanupPolicy.DELETE.name.equals(cleanupPolicyOrig)) {
            adminOperations.alterTopicConfigs(
                    topicName,
                    Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, CleanupPolicy.DELETE.name));
        }
    }

    private void restorePolicyAfterDeleteIfNeeded(String topicName, String cleanupPolicyOrig) {
        if (!CleanupPolicy.DELETE.name.equals(cleanupPolicyOrig)) {
            adminOperations.alterTopicConfigs(
                    topicName,
                    Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicyOrig));
        }
    }

}
