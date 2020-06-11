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
package com.epam.eco.kafkamanager.rest.request;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Naira_Tamrazyan
 */
public class TopicOffsetForTimeFetchRequest {

    private final String topicName;
    private final Long timestamp;

    public TopicOffsetForTimeFetchRequest(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("timestamp") Long timestamp) {
        this.topicName = topicName;
        this.timestamp = timestamp;
    }

    public String getTopicName() {
        return topicName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicOffsetForTimeFetchRequest that = (TopicOffsetForTimeFetchRequest) o;
        return Objects.equals(topicName, that.topicName) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, timestamp);
    }

    @Override
    public String toString() {
        return "TopicOffsetForTimeFetchRequest{" +
                "topicName='" + topicName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
