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
 * @author Raman_Babich
 */
public class TopicPartitionsRequest {

    private final Integer newPartitionCount;

    public TopicPartitionsRequest(
            @JsonProperty("newPartitionCount") Integer newPartitionCount) {
        this.newPartitionCount = newPartitionCount;
    }

    public Integer getNewPartitionCount() {
        return newPartitionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicPartitionsRequest that = (TopicPartitionsRequest) o;
        return Objects.equals(newPartitionCount, that.newPartitionCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPartitionCount);
    }

    @Override
    public String toString() {
        return "TopicPartitionsRequest{" +
                "newPartitionCount=" + newPartitionCount +
                '}';
    }
}
