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
public class TopicConfigUpdateParams {

    private final String topicName;
    private final Map<String, String> config;

    public TopicConfigUpdateParams(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("config") Map<String, String> config) {
        Validate.notBlank(topicName, "Topic name is blank");

        this.topicName = topicName;
        this.config =
                config != null ?
                Collections.unmodifiableMap(new HashMap<>(config)) :
                Collections.emptyMap();
    }

    public String getTopicName() {
        return topicName;
    }
    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicConfigUpdateParams that = (TopicConfigUpdateParams) obj;
        return
                Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, config);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                ", config: " + config +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TopicConfigUpdateParams origin) {
        return new Builder(origin);
    }

    public static TopicConfigUpdateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicConfigUpdateParams.class);
    }

    public static TopicConfigUpdateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicConfigUpdateParams.class);
    }

    public static class Builder {

        private String topicName;
        private Map<String, String> config = new HashMap<>();

        private Builder(TopicConfigUpdateParams origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
            this.config.putAll(origin.config);
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder appendConfigEntry(String key, String value) {
            this.config.put(key, value);
            return this;
        }

        public Builder config(Map<String, String> config) {
            this.config.clear();
            if (config != null) {
                this.config.putAll(config);
            }
            return this;
        }

        public TopicConfigUpdateParams build() {
            return new TopicConfigUpdateParams(topicName, config);
        }
    }

}
