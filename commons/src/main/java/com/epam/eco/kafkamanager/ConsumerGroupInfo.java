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
package com.epam.eco.kafkamanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.KafkaUtils;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupInfo implements MetadataAware, Comparable<ConsumerGroupInfo> {

    private final String name;
    private final Integer coordinator;
    private final ConsumerGroupState state;
    private final String protocolType;
    private final String partitionAssignor;
    private final List<ConsumerGroupMemberInfo> members;
    private final List<String> topicNames;
    private final Map<TopicPartition, OffsetAndMetadataInfo> offsetsAndMetadata;
    private final Map<TopicPartition, Long> offsets;
    private final Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries;
    private final StorageType storageType;
    private final Metadata metadata;

    public ConsumerGroupInfo(
            @JsonProperty("name") String name,
            @JsonProperty("coordinator") Integer coordinator,
            @JsonProperty("state") ConsumerGroupState state,
            @JsonProperty("protocolType") String protocolType,
            @JsonProperty("partitionAssignor") String partitionAssignor,
            @JsonProperty("members") Collection<ConsumerGroupMemberInfo> members,
            @JsonProperty("offsetsAndMetadata") Map<TopicPartition, OffsetAndMetadataInfo> offsetsAndMetadata,
            @JsonProperty("offsetTimeSeries") Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries,
            @JsonProperty("storageType") StorageType storageType,
            @JsonProperty("metadata") Metadata metadata) {
        Validate.notBlank(name, "Name is blank");
        if (members != null) {
            Validate.noNullElements(members, "Collection of members contains null elements");
        }
        if (offsetsAndMetadata != null) {
            Validate.noNullElements(
                    offsetsAndMetadata.keySet(), "Map of offsets contains null keys");
            Validate.noNullElements(
                    offsetsAndMetadata.values(), "Map of offsets contains null values");
        }
        if (offsetTimeSeries != null) {
            Validate.noNullElements(
                    offsetTimeSeries.keySet(), "Map of offset timeseries contains null keys");
            Validate.noNullElements(
                    offsetTimeSeries.values(), "Map of offset timeseries contains null values");
        }

        Validate.notNull(storageType, "Storage type is null");

        this.name = name;
        this.coordinator = coordinator;
        this.state = state;
        this.protocolType = protocolType;
        this.partitionAssignor = partitionAssignor;
        this.members =
                !CollectionUtils.isEmpty(members) ?
                members.stream().
                        sorted().
                        collect(
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        Collections::unmodifiableList)) :
                Collections.emptyList();
        this.offsetsAndMetadata =
                !MapUtils.isEmpty(offsetsAndMetadata) ?
                Collections.unmodifiableMap(
                        KafkaUtils.sortedByTopicPartitionKeyMap(offsetsAndMetadata)) :
                Collections.emptyMap();
        this.offsets =
                !MapUtils.isEmpty(offsetsAndMetadata) ?
                Collections.unmodifiableMap(
                        KafkaUtils.sortedByTopicPartitionKeyMap(
                                offsetsAndMetadata.entrySet().stream().
                                    collect(
                                            Collectors.toMap(
                                                    entry -> entry.getKey(),
                                                    entry -> entry.getValue().getOffset())))) :
                Collections.emptyMap();
        this.offsetTimeSeries =
                !MapUtils.isEmpty(offsetTimeSeries) ?
                Collections.unmodifiableMap(new HashMap<>(offsetTimeSeries)) :
                Collections.emptyMap();
        this.topicNames =
                Collections.unmodifiableList(
                        KafkaUtils.extractTopicNamesAsSortedList(this.offsets.keySet()));
        this.storageType = storageType;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }
    public Integer getCoordinator() {
        return coordinator;
    }
    public ConsumerGroupState getState() {
        return state;
    }
    public String getProtocolType() {
        return protocolType;
    }
    public String getPartitionAssignor() {
        return partitionAssignor;
    }
    public List<ConsumerGroupMemberInfo> getMembers() {
        return members;
    }
    public List<String> getTopicNames() {
        return topicNames;
    }
    public Map<TopicPartition, Long> getOffsets() {
        return offsets;
    }
    public Long getOffset(TopicPartition partition) {
        return offsets.get(partition);
    }
    public Map<TopicPartition, OffsetAndMetadataInfo> getOffsetsAndMetadata() {
        return offsetsAndMetadata;
    }
    public OffsetAndMetadataInfo getOffsetAndMetadata(TopicPartition partition) {
        return offsetsAndMetadata.get(partition);
    }
    public Map<TopicPartition, OffsetTimeSeries> getOffsetTimeSeries() {
        return offsetTimeSeries;
    }
    public OffsetTimeSeries getOffsetTimeSeries(TopicPartition partition) {
        return offsetTimeSeries.get(partition);
    }
    public StorageType getStorageType() {
        return storageType;
    }
    @Override
    public Optional<Metadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                coordinator,
                state,
                protocolType,
                partitionAssignor,
                members,
                topicNames,
                offsetsAndMetadata,
                offsetTimeSeries,
                storageType,
                metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ConsumerGroupInfo that = (ConsumerGroupInfo)obj;
        return
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.coordinator, that.coordinator) &&
                Objects.equals(this.state, that.state) &&
                Objects.equals(this.protocolType, that.protocolType) &&
                Objects.equals(this.partitionAssignor, that.partitionAssignor) &&
                Objects.equals(this.members, that.members) &&
                Objects.equals(this.topicNames, that.topicNames) &&
                Objects.equals(this.offsetsAndMetadata, that.offsetsAndMetadata) &&
                Objects.equals(this.offsetTimeSeries, that.offsetTimeSeries) &&
                Objects.equals(this.storageType, that.storageType) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public String toString() {
        return
                "{name: " + name +
                ", coordinator: " + coordinator +
                ", state: " + state +
                ", protocolType: " + protocolType +
                ", partitionAssignor: " + partitionAssignor +
                ", members: " + members +
                ", topicNames: " + topicNames +
                ", offsetsAndMetadata: " + offsetsAndMetadata +
                ", offsetTimeSeries: " + offsetTimeSeries +
                ", storageType: " + storageType +
                ", metadata: " + metadata +
                "}";
    }

    @Override
    public int compareTo(ConsumerGroupInfo that) {
        int result = ObjectUtils.compare(this.name, that.name);
        if (result == 0) {
            result = ObjectUtils.compare(this.storageType, that.storageType);
        }
        return result;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private Integer coordinator;
        private ConsumerGroupState state;
        private String protocolType;
        private String partitionAssignor;
        private List<ConsumerGroupMemberInfo> members = new ArrayList<>();
        private Map<TopicPartition, OffsetAndMetadataInfo> offsetsAndMetadata = new HashMap<>();
        private Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries = new HashMap<>();
        private StorageType storageType;
        private Metadata metadata;

        public Builder() {
            this(null);
        }

        public Builder(ConsumerGroupInfo origin) {
            if (origin == null) {
                return;
            }

            this.name = origin.name;
            this.coordinator = origin.coordinator;
            this.state = origin.state;
            this.protocolType = origin.protocolType;
            this.partitionAssignor = origin.partitionAssignor;
            this.members.addAll(origin.members);
            this.offsetsAndMetadata.putAll(origin.offsetsAndMetadata);
            this.offsetTimeSeries.putAll(origin.getOffsetTimeSeries());
            this.storageType = origin.storageType;
            this.metadata = origin.metadata;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder coordinator(Integer coordinator) {
            this.coordinator = coordinator;
            return this;
        }
        public Builder state(ConsumerGroupState state) {
            this.state = state;
            return this;
        }
        public Builder protocolType(String protocolType) {
            this.protocolType = protocolType;
            return this;
        }
        public Builder partitionAssignor(String partitionAssignor) {
            this.partitionAssignor = partitionAssignor;
            return this;
        }
        public Builder addMember(ConsumerGroupMemberInfo member) {
            this.members.add(member);
            return this;
        }
        public Builder removeMemberById(String memberId) {
            Iterator<ConsumerGroupMemberInfo> iterator = this.members.iterator();
            while (iterator.hasNext()) {
                ConsumerGroupMemberInfo member = iterator.next();
                if (member.getMemberId().equals(memberId)) {
                    iterator.remove();
                    break;
                }
            }
            return this;
        }
        public Builder members(List<ConsumerGroupMemberInfo> members) {
            this.members.clear();
            if (members != null) {
                this.members.addAll(members);
            }
            return this;
        }
        public Builder addOffsetsAndMetadata(OffsetAndMetadataInfo offsetsAndMetadata) {
            this.offsetsAndMetadata.put(offsetsAndMetadata.getTopicPartition(), offsetsAndMetadata);
            return this;
        }
        public Builder removeOffsetsAndMetadata(TopicPartition topicPartition) {
            this.offsetsAndMetadata.remove(topicPartition);
            return this;
        }
        public Builder offsetsAndMetadata(Map<TopicPartition, OffsetAndMetadataInfo> offsetsAndMetadata) {
            this.offsetsAndMetadata.clear();
            if (offsetsAndMetadata != null) {
                this.offsetsAndMetadata.putAll(offsetsAndMetadata);
            }
            return this;
        }
        public Builder offsetTimeSeries(Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries) {
            this.offsetTimeSeries.clear();
            if (offsetTimeSeries != null) {
                this.offsetTimeSeries.putAll(offsetTimeSeries);
            }
            return this;
        }
        public Builder storageType(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public ConsumerGroupInfo build() {
            return new ConsumerGroupInfo(
                    name,
                    coordinator,
                    state,
                    protocolType,
                    partitionAssignor,
                    members,
                    offsetsAndMetadata,
                    offsetTimeSeries,
                    storageType,
                    metadata);
        }

    }

    public static enum StorageType {
        KAFKA, ZOOKEEPER
    }

}
