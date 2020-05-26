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
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicSearchCriteria implements SearchCriteria<TopicInfo> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicSearchCriteria.class);

    public enum ReplicationState {
        ANY_REPLICATED, FULLY_REPLICATED, UNDER_REPLICATED
    }

    private final String topicName;
    private final Integer minPartitionCount;
    private final Integer maxPartitionCount;
    private final Integer minReplicationFactor;
    private final Integer maxReplicationFactor;
    private final Integer minConsumerCount;
    private final Integer maxConsumerCount;
    private final ReplicationState replicationState;
    private final String configString;
    private final Map<String, String> configMap;
    private final String description;

    private final transient KafkaManager kafkaManager;

    public TopicSearchCriteria(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("minPartitionCount") Integer minPartitionCount,
            @JsonProperty("maxPartitionCount") Integer maxPartitionCount,
            @JsonProperty("minReplicationFactor") Integer minReplicationFactor,
            @JsonProperty("maxReplicationFactor") Integer maxReplicationFactor,
            @JsonProperty("minConsumerCount") Integer minConsumerCount,
            @JsonProperty("maxConsumerCount") Integer maxConsumerCount,
            @JsonProperty("replicationState") ReplicationState replicationState,
            @JsonProperty("configString") String configString,
            @JsonProperty("configMap") Map<String, String> configMap,
            @JsonProperty("description") String description) {
        this(
                topicName,
                minPartitionCount,
                maxPartitionCount,
                minReplicationFactor,
                maxReplicationFactor,
                minConsumerCount,
                maxConsumerCount,
                replicationState,
                configString,
                configMap,
                description,
                null);
    }

    public TopicSearchCriteria(
            String topicName,
            Integer minPartitionCount,
            Integer maxPartitionCount,
            Integer minReplicationFactor,
            Integer maxReplicationFactor,
            Integer minConsumerCount,
            Integer maxConsumerCount,
            ReplicationState replicationState,
            String configString,
            Map<String, String> configMap,
            String description,
            KafkaManager kafkaManager) {
        this.topicName = topicName;
        this.minPartitionCount = minPartitionCount;
        this.maxPartitionCount = maxPartitionCount;
        this.minReplicationFactor = minReplicationFactor;
        this.maxReplicationFactor = maxReplicationFactor;
        this.minConsumerCount = minConsumerCount;
        this.maxConsumerCount = maxConsumerCount;
        this.replicationState = replicationState;
        this.configString = configString;
        this.configMap =
                configMap != null ? Collections.unmodifiableMap(new HashMap<>(configMap)) : null;
        this.description = description;
        this.kafkaManager = kafkaManager;
    }

    public String getTopicName() {
        return topicName;
    }
    public Integer getMinPartitionCount() {
        return minPartitionCount;
    }
    public Integer getMaxPartitionCount() {
        return maxPartitionCount;
    }
    public Integer getMinReplicationFactor() {
        return minReplicationFactor;
    }
    public Integer getMaxReplicationFactor() {
        return maxReplicationFactor;
    }
    public Integer getMinConsumerCount() {
        return minConsumerCount;
    }
    public Integer getMaxConsumerCount() {
        return maxConsumerCount;
    }
    public ReplicationState getReplicationState() {
        return replicationState;
    }
    public String getConfigString() {
        return configString;
    }
    public Map<String, String> getConfigMap() {
        return configMap;
    }
    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public KafkaManager getKafkaManager() {
        return kafkaManager;
    }

    @Override
    public boolean matches(TopicInfo obj) {
        Validate.notNull(obj, "Topic Info is null");

        Boolean underReplicated = null;
        if (ReplicationState.FULLY_REPLICATED == replicationState) {
            underReplicated = Boolean.FALSE;
        } else if (ReplicationState.UNDER_REPLICATED == replicationState) {
            underReplicated = Boolean.TRUE;
        }

        return
                (StringUtils.isBlank(topicName) || StringUtils.containsIgnoreCase(obj.getName(), topicName)) &&
                (minPartitionCount == null || obj.getPartitionCount() >= minPartitionCount) &&
                (maxPartitionCount == null || obj.getPartitionCount() <= maxPartitionCount) &&
                (minReplicationFactor == null || obj.getReplicationFactor() >= minReplicationFactor) &&
                (maxReplicationFactor == null || obj.getReplicationFactor() <= maxReplicationFactor) &&
                (minConsumerCount == null || matchesMinConsumerCount(obj)) &&
                (maxConsumerCount == null || matchesMaxConsumerCount(obj)) &&
                (underReplicated == null || obj.hasUnderReplicatedPartitions() == underReplicated) &&
                (
                        StringUtils.isBlank(configString) ||
                        obj.getConfig().entrySet().containsAll(parseConfigString(configString).entrySet())) &&
                (configMap == null || obj.getConfig().entrySet().containsAll(configMap.entrySet())) &&
                (StringUtils.isBlank(description) || StringUtils.containsIgnoreCase(
                        obj.getMetadata().map(Metadata::getDescription).orElse(null), description));
    }

    private boolean matchesMinConsumerCount(TopicInfo obj) {
        if (kafkaManager != null) {
            return kafkaManager.getConsumerGroupsForTopic(obj.getName()).size() >= minConsumerCount;
        }
        LOGGER.warn("Constraint 'minConsumerCount' is ignored, because kafkaManager is null.");
        return true;
    }

    private boolean matchesMaxConsumerCount(TopicInfo obj) {
        if (kafkaManager != null) {
            return kafkaManager.getConsumerGroupsForTopic(obj.getName()).size() <= maxConsumerCount;
        }
        LOGGER.warn("Constraint 'maxConsumerCount' is ignored, because kafkaManager is null.");
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicSearchCriteria that = (TopicSearchCriteria) obj;
        return
                Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.minPartitionCount, that.minPartitionCount) &&
                Objects.equals(this.maxPartitionCount, that.maxPartitionCount) &&
                Objects.equals(this.minReplicationFactor, that.minReplicationFactor) &&
                Objects.equals(this.maxReplicationFactor, that.maxReplicationFactor) &&
                Objects.equals(this.minConsumerCount, that.minConsumerCount) &&
                Objects.equals(this.maxConsumerCount, that.maxConsumerCount) &&
                Objects.equals(this.replicationState, that.replicationState) &&
                Objects.equals(this.configString, that.configString) &&
                Objects.equals(this.configMap, that.configMap) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                topicName,
                minPartitionCount,
                maxPartitionCount,
                minReplicationFactor,
                maxReplicationFactor,
                minConsumerCount,
                maxConsumerCount,
                replicationState,
                configString,
                configMap,
                description);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                ", minPartitionCount: " + minPartitionCount +
                ", maxPartitionCount: " + maxPartitionCount +
                ", minReplicationFactor: " + minReplicationFactor +
                ", maxReplicationFactor: " + maxReplicationFactor +
                ", minConsumerCount: " + minConsumerCount +
                ", maxConsumerCount: " + maxConsumerCount +
                ", replicationState: " + replicationState +
                ", configString: " + configString +
                ", configMap: " + configMap +
                ", description: " + description +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TopicSearchCriteria origin) {
        return new Builder(origin);
    }

    public static TopicSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicSearchCriteria.class);
    }

    public static TopicSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicSearchCriteria.class);
    }

    public static TopicSearchCriteria fromJsonWith(Map<String, ?> map, KafkaManager kafkaManager) {
        TopicSearchCriteria topicSearchCriteria = fromJson(map);
        if (topicSearchCriteria == null) {
            return null;
        }
        return topicSearchCriteria.toBuilder()
                .kafkaManager(kafkaManager)
                .build();
    }

    public static TopicSearchCriteria fromJsonWith(String json, KafkaManager kafkaManager) {
        TopicSearchCriteria topicSearchCriteria = fromJson(json);
        if (topicSearchCriteria == null) {
            return null;
        }
        return topicSearchCriteria.toBuilder()
                .kafkaManager(kafkaManager)
                .build();
    }

    static Map<String, String> parseConfigString(String configString) {
        String config = StringUtils.stripToNull(configString);
        if (config == null) {
            return Collections.emptyMap();
        }

        String[] parts = StringUtils.split(config, ";");
        Map<String, String> configMap = new HashMap<>((int) Math.ceil(parts.length / 0.75));
        for (String configEntry : parts) {
            int colonIdx = configEntry.indexOf(':');
            String key =
                    colonIdx >= 0 ?
                            StringUtils.stripToNull(configEntry.substring(0, colonIdx)) :
                            null;
            String value =
                    colonIdx >= 0 ?
                            StringUtils.stripToNull(configEntry.substring(colonIdx + 1)) :
                            null;
            configMap.put(key, value);
        }
        return configMap;
    }

    public static class Builder {

        private String topicName;
        private Integer minPartitionCount;
        private Integer maxPartitionCount;
        private Integer minReplicationFactor;
        private Integer maxReplicationFactor;
        private Integer minConsumerCount;
        private Integer maxConsumerCount;
        private ReplicationState replicationState = ReplicationState.ANY_REPLICATED;
        private String configString;
        private Map<String, String> configMap;
        private String description;

        private KafkaManager kafkaManager;

        private Builder(TopicSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
            this.minPartitionCount = origin.minPartitionCount;
            this.maxPartitionCount = origin.maxPartitionCount;
            this.minReplicationFactor = origin.minReplicationFactor;
            this.maxReplicationFactor = origin.maxReplicationFactor;
            this.minConsumerCount = origin.minConsumerCount;
            this.maxConsumerCount = origin.maxConsumerCount;
            this.replicationState = origin.replicationState;
            this.configString = origin.configString;
            this.configMap = origin.configMap;
            this.description = origin.description;
            this.kafkaManager = origin.kafkaManager;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder minPartitionCount(Integer minPartitionCount) {
            this.minPartitionCount = minPartitionCount;
            return this;
        }

        public Builder maxPartitionCount(Integer maxPartitionCount) {
            this.maxPartitionCount = maxPartitionCount;
            return this;
        }

        public Builder minReplicationFactor(Integer minReplicationFactor) {
            this.minReplicationFactor = minReplicationFactor;
            return this;
        }

        public Builder maxReplicationFactor(Integer maxReplicationFactor) {
            this.maxReplicationFactor = maxReplicationFactor;
            return this;
        }

        public Builder minConsumerCount(Integer minConsumerCount) {
            this.minConsumerCount = minConsumerCount;
            return this;
        }

        public Builder maxConsumerCount(Integer maxConsumerCount) {
            this.maxConsumerCount = maxConsumerCount;
            return this;
        }

        public Builder replicationStateAny() {
            return replicationState(ReplicationState.ANY_REPLICATED);
        }

        public Builder replicationStateFully() {
            return replicationState(ReplicationState.FULLY_REPLICATED);
        }

        public Builder replicationStateUnder() {
            return replicationState(ReplicationState.UNDER_REPLICATED);
        }

        public Builder replicationState(ReplicationState replicationState) {
            this.replicationState = replicationState;
            return this;
        }

        public Builder configString(String configString) {
            this.configString = configString;
            return this;
        }

        public Builder configMap(Map<String, String> configMap) {
            this.configMap = configMap;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder kafkaManager(KafkaManager kafkaManager) {
            this.kafkaManager = kafkaManager;
            return this;
        }

        public TopicSearchCriteria build() {
            return new TopicSearchCriteria(
                    topicName,
                    minPartitionCount,
                    maxPartitionCount,
                    minReplicationFactor,
                    maxReplicationFactor,
                    minConsumerCount,
                    maxConsumerCount,
                    replicationState,
                    configString,
                    configMap,
                    description,
                    kafkaManager);
        }

    }

}
