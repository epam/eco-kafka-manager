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
package com.epam.eco.kafkamanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.KafkaUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicInfo implements MetadataAware, Comparable<TopicInfo> {

    private final String name;
    private final Map<TopicPartition, PartitionInfo> partitions;
    private final Map<String, ConfigValue> config;
    private final Map<String, String> configOverrides;
    private final Metadata metadata;

    private final List<TopicPartition> underReplicatedPartitions;
    private final int partitionCount;
    private final int replicationFactor;

    public TopicInfo(
            @JsonProperty("name") String name,
            @JsonProperty("partitions") Map<TopicPartition, PartitionInfo> partitions,
            @JsonProperty("config") Map<String, ConfigValue> config,
            @JsonProperty("metadata") Metadata metadata) {
        Validate.notBlank(name, "Name is blank");
        Validate.notEmpty(partitions, "Partitions map is null or empty");
        Validate.noNullElements(partitions.keySet(), "Partitions map contains null keys");
        Validate.noNullElements(partitions.values(), "Partitions map contains null values");
        Validate.notEmpty(config, "Config map is null or empty");
        Validate.noNullElements(config.keySet(), "Collection of config keys contains null elements");
        Validate.noNullElements(config.values(), "Collection of config values contains null elements");

        this.name = name;
        this.partitions = Collections.unmodifiableMap(
                KafkaUtils.sortedByTopicPartitionKeyMap(partitions));
        this.config = Collections.unmodifiableMap(new TreeMap<>(config));
        this.metadata = metadata;

        underReplicatedPartitions = calculateUnderReplicatedPartitions();
        configOverrides = calculateConfigOverrides();
        partitionCount = calculatePartitionCount();
        replicationFactor = calculateReplicationFactor();
    }

    public String getName() {
        return name;
    }
    public int getPartitionCount() {
        return partitionCount;
    }
    public int getReplicationFactor() {
        return replicationFactor;
    }
    public Map<TopicPartition, PartitionInfo> getPartitions() {
        return partitions;
    }
    public PartitionInfo getPartition(int partition) {
        return partitions.get(new TopicPartition(name, partition));
    }
    public Map<String, ConfigValue> getConfig() {
        return config;
    }
    public List<TopicPartition> getUnderReplicatedPartitions() {
        return underReplicatedPartitions;
    }
    public boolean hasUnderReplicatedPartitions() {
        return !underReplicatedPartitions.isEmpty();
    }
    public Map<String, String> getConfigOverrides() {
        return configOverrides;
    }
    @Override
    public Optional<Metadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, partitions, config, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TopicInfo that = (TopicInfo)obj;
        return
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.partitions, that.partitions) &&
                Objects.equals(this.config, that.config) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public String toString() {
        return
                "{name: " + name +
                ", partitions: " + partitions +
                ", config: " + config +
                ", metadata: " + metadata +
                "}";
    }

    @Override
    public int compareTo(TopicInfo that) {
        return ObjectUtils.compare(this.name, that.name);
    }

    private List<TopicPartition> calculateUnderReplicatedPartitions() {
        return partitions.values().stream().
                filter(PartitionInfo::isUnderReplicated).
                map(PartitionInfo::getId).
                collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList));
    }

    private Map<String, String> calculateConfigOverrides() {
        return config.values().stream().
                filter(v -> !v.isDefault()).
                collect(
                        Collectors.collectingAndThen(
                                Collectors.toMap(
                                        ConfigValue::getName,
                                        ConfigValue::getValue,
                                        (v0,v1) -> v0,
                                        TreeMap::new),
                                Collections::unmodifiableMap));
    }

    private int calculatePartitionCount() {
        return partitions.size();
    }

    private int calculateReplicationFactor() {
        return partitions.values().iterator().next().getReplicas().size();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private Map<TopicPartition, PartitionInfo> partitions = new HashMap<>();
        private Map<String, ConfigValue> config = new HashMap<>();
        private Metadata metadata;

        public Builder() {
            this(null);
        }

        public Builder(TopicInfo origin) {
            if (origin == null) {
                return;
            }

            this.name = origin.name;
            this.partitions.putAll(origin.partitions);
            this.config.putAll(origin.config);
            this.metadata = origin.metadata;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder partitions(Map<TopicPartition, PartitionInfo> partitions) {
            this.partitions = partitions;
            return this;
        }
        public Builder config(Map<String, ConfigValue> config) {
            this.config = config;
            return this;
        }
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public TopicInfo build() {
            return new TopicInfo(
                    name,
                    partitions,
                    config,
                    metadata);
        }

    }

}
