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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class TopicPartitionsCreateParams {

    private final String topicName;
    private final int newPartitionCount;

    public TopicPartitionsCreateParams(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("newPartitionCount") int newPartitionCount) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(newPartitionCount > 0, "New Partition Count is invalid");

        this.topicName = topicName;
        this.newPartitionCount = newPartitionCount;
    }

    public String getTopicName() {
        return topicName;
    }
    public int getNewPartitionCount() {
        return newPartitionCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicPartitionsCreateParams that = (TopicPartitionsCreateParams) obj;
        return
                Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.newPartitionCount, that.newPartitionCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, newPartitionCount);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                ", newPartitionCount: " + newPartitionCount +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TopicPartitionsCreateParams origin) {
        return new Builder(origin);
    }

    public static TopicPartitionsCreateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicPartitionsCreateParams.class);
    }

    public static TopicPartitionsCreateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicPartitionsCreateParams.class);
    }

    public static class Builder {

        private String topicName;
        private int newPartitionCount;

        private Builder(TopicPartitionsCreateParams origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
            this.newPartitionCount = origin.newPartitionCount;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder newPartitionCount(int newPartitionCount) {
            this.newPartitionCount = newPartitionCount;
            return this;
        }

        public TopicPartitionsCreateParams build() {
            return new TopicPartitionsCreateParams(topicName, newPartitionCount);
        }
    }

}
