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
package com.epam.eco.kafkamanager.ui.consumers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.ConsumerGroupMemberInfo;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.OffsetAndMetadataInfo;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupInfoWrapper {

    private final ConsumerGroupInfo groupInfo;

    private final Map<TopicPartition, Long> offsetRpms = new HashMap<>();

    public ConsumerGroupInfoWrapper(ConsumerGroupInfo groupInfo) {
        Validate.notNull(groupInfo, "Consumer group info is null");

        this.groupInfo = groupInfo;
    }

    public static ConsumerGroupInfoWrapper wrap(ConsumerGroupInfo consumerGroupInfo) {
        return new ConsumerGroupInfoWrapper(consumerGroupInfo);
    }

    public String getName() {
        return groupInfo.getName();
    }

    public ConsumerGroupState getState() {
        return groupInfo.getState();
    }

    public String getProtocolType() {
        return groupInfo.getProtocolType();
    }

    public String getPartitionAssignor() {
        return groupInfo.getPartitionAssignor();
    }

    public boolean hasMembers() {
        return !groupInfo.getMembers().isEmpty();
    }

    public List<ConsumerGroupMemberInfo> getMembers() {
        return groupInfo.getMembers();
    }

    public List<String> getTopicNames() {
        return groupInfo.getTopicNames();
    }

    public boolean hasOffsets() {
        return !groupInfo.getOffsetsAndMetadata().isEmpty();
    }

    public Map<TopicPartition, Long> getOffsets() {
        return groupInfo.getOffsets();
    }

    public List<OffsetAndMetadataInfo> getOffsetsAndMetadataAsList() {
        return groupInfo.getOffsetsAndMetadata().values().stream().
                sorted().
                collect(Collectors.toList());
    }

    public Long getOffsetRpm(TopicPartition topicPartition) {
        return offsetRpms.computeIfAbsent(topicPartition, this::calculateOffsetRpm);
    }

    private Long calculateOffsetRpm(TopicPartition topicPartition) {
        OffsetTimeSeries timeSeries =
                groupInfo.getOffsetTimeSeries().get(topicPartition);
        return timeSeries != null ? timeSeries.currentRatePerMinute() : null;
    }

    public StorageType getStorageType() {
        return groupInfo.getStorageType();
    }

    public String getMetadataDescription() {
        return groupInfo.getMetadata().map(Metadata::getDescription).orElse(null);
    }

    public CollapsedCollectionIterable<String> getTopicNamesAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                groupInfo.getTopicNames(),
                size);
    }

    public CollapsedCollectionIterable<String> getMembersAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                groupInfo.getMembers(),
                m -> String.format("%s(%s)", m.getClientId(), m.getClientHost() != null ? m.getClientHost() : ""),
                size);
    }

    public CollapsedCollectionIterable<String> getOffsetsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                groupInfo.getOffsets().entrySet(),
                e -> String.format("%s = %s", e.getKey(), e.getValue() != null ? e.getValue() : "N/A"),
                size);
    }

}
