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
package com.epam.eco.kafkamanager.rest.request;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.TopicRecordFetchParams;

/**
 * @author Raman_Babich
 */
public class TopicRecordFetchRequest {

    private final String topicName;
    private final TopicRecordFetchParams fetchParams;

    public TopicRecordFetchRequest(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("fetchParams") TopicRecordFetchParams fetchParams) {//TODO maybe undo this change for now?
        this.topicName = topicName;
        this.fetchParams = fetchParams;
    }

    public String getTopicName() {
        return topicName;
    }

    public TopicRecordFetchParams getFetchParams() {
        return fetchParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicRecordFetchRequest that = (TopicRecordFetchRequest) o;
        return Objects.equals(topicName, that.topicName) &&
                Objects.equals(fetchParams, that.fetchParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, fetchParams);
    }

    @Override
    public String toString() {
        return "TopicRecordFetchRequest{" +
                "topicName='" + topicName + '\'' +
                ", fetchParams=" + fetchParams +
                '}';
    }
}
