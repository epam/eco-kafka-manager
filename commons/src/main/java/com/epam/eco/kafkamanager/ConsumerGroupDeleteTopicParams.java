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
public class ConsumerGroupDeleteTopicParams {

    private final String groupName;
    private final String topicName;

    public ConsumerGroupDeleteTopicParams(
            @JsonProperty("groupName") String groupName,
            @JsonProperty("topicName") String topicName) {
        Validate.notBlank(groupName, "Group Name is blank");
        Validate.notBlank(topicName, "Topic Name is blank");

        this.groupName = groupName;
        this.topicName = topicName;
    }

    public String getGroupName() {
        return groupName;
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
        ConsumerGroupDeleteTopicParams that = (ConsumerGroupDeleteTopicParams) obj;
        return
                Objects.equals(this.groupName, that.groupName) &&
                Objects.equals(this.topicName, that.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, topicName);
    }

    @Override
    public String toString() {
        return
                "{groupName: " + groupName +
                ", topicName: " + topicName +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(ConsumerGroupDeleteTopicParams origin) {
        return new Builder(origin);
    }

    public static ConsumerGroupDeleteTopicParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, ConsumerGroupDeleteTopicParams.class);
    }

    public static ConsumerGroupDeleteTopicParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, ConsumerGroupDeleteTopicParams.class);
    }

    public static class Builder {

        private String groupName;
        private String topicName;

        private Builder(ConsumerGroupDeleteTopicParams origin) {
            if (origin == null) {
                return;
            }

            this.groupName = origin.groupName;
            this.topicName = origin.topicName;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public ConsumerGroupDeleteTopicParams build() {
            return new ConsumerGroupDeleteTopicParams(groupName, topicName);
        }
    }

}
