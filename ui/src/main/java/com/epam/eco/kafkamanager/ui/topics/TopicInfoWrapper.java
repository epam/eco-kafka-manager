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
package com.epam.eco.kafkamanager.ui.topics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.OffsetAndMetadataInfo;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.PartitionInfo;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.common.ConfigEntryWrapper;
import com.epam.eco.kafkamanager.ui.consumers.ConsumerGroupInfoWrapper;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;
import com.epam.eco.kafkamanager.ui.utils.WorkerUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicInfoWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicInfoWrapper.class);

    private final TopicInfo topicInfo;
    private final KafkaManager kafkaManager;
    private final KafkaAdminOperations adminOperations;

    private Map<TopicPartition, OffsetRange> offsets;
    private final Map<TopicPartition, Long> offsetRpms = new HashMap<>();

    private Map<String, ConfigEntryWrapper> allConfigEntries;

    public TopicInfoWrapper(
            String topicName,
            KafkaManager kafkaManager,
            KafkaAdminOperations adminOperations) {
        this(kafkaManager.getTopic(topicName), kafkaManager, adminOperations);
    }

    public TopicInfoWrapper(
            TopicInfo topicInfo,
            KafkaManager kafkaManager,
            KafkaAdminOperations adminOperations) {
        Validate.notNull(topicInfo, "Topic info is null");
        Validate.notNull(kafkaManager, "Kafka manager can't be null");
        Validate.notNull(adminOperations, "AdminOperations can't be null");

        this.topicInfo = topicInfo;
        this.kafkaManager = kafkaManager;
        this.adminOperations = adminOperations;
    }

    public static TopicInfoWrapper wrap(
            TopicInfo topicInfo,
            KafkaManager kafkaManager,
            KafkaAdminOperations adminOperations) {
        return new TopicInfoWrapper(topicInfo, kafkaManager, adminOperations);
    }

    public static TopicInfoWrapper wrap(
            String topicName,
            KafkaManager kafkaManager,
            KafkaAdminOperations adminOperations) {
        return new TopicInfoWrapper(topicName, kafkaManager, adminOperations);
    }

    public Map<String, String> getConfig() {
        return topicInfo.getConfig();
    }

    public Map<String, ConfigEntryWrapper> getAllConfigEntries() {
        if (allConfigEntries == null) {
            Config config = adminOperations.describeTopicConfig(getName());
            allConfigEntries = new TreeMap<>();
            config.entries().forEach(
                    e -> allConfigEntries.put(e.name(), ConfigEntryWrapper.wrapForTopic(e)));
        }
        return allConfigEntries;
    }

    public String getName() {
        return topicInfo.getName();
    }

    public String getMetadataDescription() {
        return topicInfo.getMetadata().map(Metadata::getDescription).orElse(null);
    }

    public Metadata getMetadata() {
        return topicInfo.getMetadata().orElse(null);
    }

    public Map<TopicPartition, PartitionInfo> getPartitions() {
        return topicInfo.getPartitions();
    }

    public List<TopicPartition> getUnderReplicatedPartitions() {
        return topicInfo.getUnderReplicatedPartitions();
    }

    public boolean hasUnderReplicatedPartitions() {
        return topicInfo.hasUnderReplicatedPartitions();
    }

    public OffsetRange getPartitionOffsets(TopicPartition partition) {
        return getOffsets().get(partition);
    }

    public Map<TopicPartition, OffsetRange> getOffsets() {
        if (offsets == null) {
            offsets = kafkaManager.getTopicOffsetRangeFetcherTaskExecutor().
                    getResultIfActualOrRefresh(topicInfo.getName()).
                    getValue();
        }
        return offsets;
    }

    public int getPartitionCount() {
        return topicInfo.getPartitionCount();
    }

    public int getReplicationFactor() {
        return topicInfo.getReplicationFactor();
    }

    public List<ConsumerGroupInfo> getConsumerGroups() {
        return kafkaManager.getConsumerGroupsForTopic(topicInfo.getName());
    }

    public int getConsumerGroupCount() {
        return getConsumerGroups().size();
    }

    public List<TransactionInfo> getTransactions() {
        return kafkaManager.getTransactionsForTopic(topicInfo.getName());
    }

    public boolean isCountRecordsTaskRunning() {
        try {
            return kafkaManager.getTopicRecordCounterTaskExecutor().isRunning(topicInfo.getName());
        } catch (AccessDeniedException ade) {
            return true;
        }
    }

    public String getCountRecordsTaskResultValueAsString() {
        TaskResult<Long> result = null;
        try {


            result = kafkaManager.getTopicRecordCounterTaskExecutor()
                    .getResult(topicInfo.getName())
                    .orElse(null);
        } catch (AccessDeniedException ade) {
            // null result
        }

        try {
            return result != null ? result.getValue().toString() : "N/A";
        } catch (Exception ex) {
            LOGGER.error("Failed to get 'CountRecords' task result value", ex);
            return "Error";
        }
    }

    public String getCountRecordsTaskResultAsString() {
        TaskResult<Long> result = null;
        try {
            result = kafkaManager.getTopicRecordCounterTaskExecutor()
                    .getResult(topicInfo.getName())
                    .orElse(null);
        } catch (AccessDeniedException ade) {
            // null result
        }

        if (result == null) {
            return "N/A";
        }
        String builder = "Started At: " + result.getStartedAt() + '\n' +
                "Finished At: " + result.getFinishedAt() + '\n' +
                "Elapsed: " + result.getElapsedFormattedAsHMS() + '\n' +
                "Error: " + WorkerUtils.getErrorMessageOrDefaultIfNoError(result, "-");
        return builder;
    }

    public String getConfigOverridesAsString() {
        Map<String, String> config = topicInfo.getConfig();
        if (config == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Entry<String, String> configEntry : config.entrySet()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(configEntry.getKey()).append(": ").append(configEntry.getValue());
        }
        return builder.toString();
    }

    public CollapsedCollectionIterable<String> getConsumerGroupNamesAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                getConsumerGroups(),
                ConsumerGroupInfo::getName,
                size);
    }

    public CollapsedCollectionIterable<String> getTransactionalIdsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                getTransactions(),
                TransactionInfo::getTransactionalId,
                size);
    }

    public List<TopicConsumerGroupOffsets> getConsumerGroupOffsets() {
        List<TopicConsumerGroupOffsets> consumerGroupOffsets = new ArrayList<>();
        for (ConsumerGroupInfo groupInfo : getConsumerGroups()) {
            for (TopicPartition topicPartition : groupInfo.getOffsets().keySet()) {
                OffsetRange partitionOffsets = getPartitionOffsets(topicPartition);
                if (partitionOffsets != null) {
                    ConsumerGroupInfoWrapper groupInfoWrapper = ConsumerGroupInfoWrapper.wrap(groupInfo);
                    OffsetAndMetadataInfo offsetAndMetadataInfo = groupInfo.getOffsetAndMetadata(topicPartition);

                    TopicConsumerGroupOffsets topicConsumerGroupOffsets = new TopicConsumerGroupOffsets();
                    topicConsumerGroupOffsets.setGroupName(groupInfo.getName());
                    topicConsumerGroupOffsets.setPartition(topicPartition);
                    topicConsumerGroupOffsets.setTopicOffsets(partitionOffsets);
                    topicConsumerGroupOffsets.setTopicOffsetRpm(getOffsetRpm(topicPartition));
                    topicConsumerGroupOffsets.setConsumerOffset(offsetAndMetadataInfo.getOffset());
                    topicConsumerGroupOffsets.setConsumerOffsetRpm(
                            groupInfoWrapper.getOffsetRpm(topicPartition));
                    topicConsumerGroupOffsets.setConsumerCommitDate(offsetAndMetadataInfo.getCommitDate());
                    topicConsumerGroupOffsets.setConsumerOffsetActualForMinute(
                            offsetAndMetadataInfo.isActualForMinute());

                    consumerGroupOffsets.add(topicConsumerGroupOffsets);
                }
            }
        }
        Collections.sort(consumerGroupOffsets);
        return consumerGroupOffsets;
    }

    public Long getOffsetRpm(TopicPartition topicPartition) {
        return offsetRpms.computeIfAbsent(topicPartition, this::calculateOffsetRpm);
    }

    private Long calculateOffsetRpm(TopicPartition topicPartition) {
        OffsetTimeSeries timeSeries = kafkaManager.getTopicOffsetRangeFetcherTaskExecutor().
                getOffsetTimeSeries(topicInfo.getName()).
                get(topicPartition);
        return timeSeries != null ? timeSeries.currentRatePerMinute() : null;
    }

}
