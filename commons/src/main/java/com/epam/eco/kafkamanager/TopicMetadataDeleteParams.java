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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicMetadataDeleteParams {

    private final String topicName;

    public TopicMetadataDeleteParams(
            @JsonProperty("topicName") String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicMetadataDeleteParams that = (TopicMetadataDeleteParams) obj;
        return
                Objects.equals(this.topicName, that.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TopicMetadataDeleteParams origin) {
        return new Builder(origin);
    }

    public static TopicMetadataDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicMetadataDeleteParams.class);
    }

    public static TopicMetadataDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicMetadataDeleteParams.class);
    }

    public static class Builder {

        private String topicName;

        private Builder(TopicMetadataDeleteParams origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }


        public TopicMetadataDeleteParams build() {
            return new TopicMetadataDeleteParams(topicName);
        }

    }

}
