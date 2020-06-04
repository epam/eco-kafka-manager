/*
 * Copyright 2020 EPAM Systems
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

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.helpers.TopicOffsetForTimeFetcher;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class TopicOffsetForTimeFetcherTaskExecutorImpl extends
        AbstractTaskExecutor<String, Long, Map<TopicPartition, Long>> implements TopicOffsetForTimeFetcherTaskExecutor {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    @Override
    protected TaskResult doExecute(String topicName, Long timestamp) {
        return TaskResult.of(() -> executeInternal(topicName, timestamp));
    }

    private TaskResult<Map<TopicPartition, Long>> executeInternal(String topicName, Long timestamp) {
        return TaskResult.of(() -> {
            // sanity check just for case topic doesn't exist
            kafkaManager.getTopic(topicName);

            return TopicOffsetForTimeFetcher.
                    with(properties.getCommonConsumerConfig()).
                    fetchForTopic(topicName, timestamp);
        });
    }
}
