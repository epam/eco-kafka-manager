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
package com.epam.eco.kafkamanager.core.consumer.exec;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.TopicOffsetRangeFetcher;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupTopicOffsetFetcherTaskExecutorImpl extends AbstractAsyncStatefullTaskExecutor<String, Map<TopicPartition, OffsetRange>> implements ConsumerGroupTopicOffsetFetcherTaskExecutor {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    public ConsumerGroupTopicOffsetFetcherTaskExecutorImpl(CacheManager cacheManager) {
        super(cacheManager);
    }

    public ConsumerGroupTopicOffsetFetcherTaskExecutorImpl(
            ExecutorService executor,
            CacheManager cacheManager) {
        super(executor, cacheManager);
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    @Override
    protected TaskResult<Map<TopicPartition, OffsetRange>> doExecute(String groupName) {
        return TaskResult.of(() -> {
            ConsumerGroupInfo groupInfo = kafkaManager.getConsumerGroup(groupName);
            if (groupInfo.getTopicNames().isEmpty()) {
                return Collections.emptyMap();
            }
            return TopicOffsetRangeFetcher.
                    with(properties.getCommonConsumerConfig()).
                    fetchForTopics(groupInfo.getTopicNames());
        });
    }

}
