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

import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.helpers.GroupOffsetResetter;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupOffsetResetterTaskExecutorImpl extends AbstractTaskExecutor<String, Map<TopicPartition, Long>, Void> implements ConsumerGroupOffsetResetterTaskExecutor {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    @Override
    protected TaskResult<Void> doExecute(String resourceKey, Map<TopicPartition, Long> input) {
        return TaskResult.of(() -> executeDetailedInternal(resourceKey, input));
    }

    private Void executeDetailedInternal(String groupName, Map<TopicPartition, Long> offsets) {
        ConsumerGroupInfo groupInfo = kafkaManager.getConsumerGroup(groupName);
        if (StorageType.ZOOKEEPER == groupInfo.getStorageType()) {
            throw new IllegalArgumentException(
                    "Resetting offsets for ZK-backed consumer groups is not supported");
        }
        if (offsets.isEmpty()) {
            return null;
        }

        GroupOffsetResetter.
                with(properties.getCommonConsumerConfig()).
                reset(groupName, offsets);

        return null;
    }

}
