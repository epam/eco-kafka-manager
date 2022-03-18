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
package com.epam.eco.kafkamanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicCreateParams {

    private final String topicName;
    private final int partitionCount;
    private final int replicationFactor;
    private final Map<String, String> config;
    private final String description;
    private final Map<String, Object> attributes;

    public TopicCreateParams(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("partitionCount") int partitionCount,
            @JsonProperty("replicationFactor") int replicationFactor,
            @JsonProperty("config") Map<String, String> config,
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(partitionCount > 0, "Partition Count is invalid");
        Validate.isTrue(replicationFactor > 0, "Replication Factor is invalid");

        this.topicName = topicName;
        this.partitionCount = partitionCount;
        this.replicationFactor = replicationFactor;
        this.config =
                config != null ?
                Collections.unmodifiableMap(new HashMap<>(config)) :
                Collections.emptyMap();
        this.description = description;
        this.attributes =
                attributes != null ?
                Collections.unmodifiableMap(new HashMap<>(attributes)) :
                Collections.emptyMap();
    }

    public String getTopicName() {
        return topicName;
    }
    public int getPartitionCount() {
        return partitionCount;
    }
    public int getReplicationFactor() {
        return replicationFactor;
    }
    public Map<String, String> getConfig() {
        return config;
    }
    public String getDescription() {
        return description;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicCreateParams that = (TopicCreateParams) obj;
        return
                Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.partitionCount, that.partitionCount) &&
                Objects.equals(this.replicationFactor, that.replicationFactor) &&
                Objects.equals(this.config, that.config) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                topicName,
                partitionCount,
                replicationFactor,
                config,
                description,
                attributes);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                ", partitionCount: " + partitionCount +
                ", replicationFactor: " + replicationFactor +
                ", config: " + config +
                ", description: " + description +
                ", attributes: " + attributes +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TopicCreateParams origin) {
        return new Builder(origin);
    }

    public static TopicCreateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicCreateParams.class);
    }

    public static TopicCreateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicCreateParams.class);
    }

    public static class Builder {

        private String topicName;
        private int partitionCount;
        private int replicationFactor;
        private Map<String, String> config = new HashMap<>();
        private String description;
        private Map<String, Object> attributes = new HashMap<>();

        private Builder(TopicCreateParams origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
            this.partitionCount = origin.partitionCount;
            this.replicationFactor = origin.replicationFactor;
            this.config.putAll(origin.config);
            this.description = origin.description;
            this.attributes.putAll(origin.attributes);
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder partitionCount(int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        public Builder replicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        public Builder config(Map<String, String> config) {
            this.config.clear();
            if (config != null) {
                this.config.putAll(config);
            }
            return this;
        }

        public Builder appendConfigEntry(String key, String value) {
            this.config.put(key, value);
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.clear();
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public TopicCreateParams build() {
            return new TopicCreateParams(
                    topicName,
                    partitionCount,
                    replicationFactor,
                    config,
                    description,
                    attributes);
        }
    }

}
