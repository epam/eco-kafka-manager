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

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class ConsumerGroupOffsetResetRequest {

    private final String groupName;
    private final Map<TopicPartition, Long> offsets;

    public ConsumerGroupOffsetResetRequest(
            @JsonProperty("groupName") String groupName,
            @JsonProperty("offsets") Map<TopicPartition, Long> offsets) {
        this.groupName = groupName;
        this.offsets = offsets;
    }

    public String getGroupName() {
        return groupName;
    }

    public Map<TopicPartition, Long> getOffsets() {
        return offsets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumerGroupOffsetResetRequest that = (ConsumerGroupOffsetResetRequest) o;
        return Objects.equals(groupName, that.groupName) &&
                Objects.equals(offsets, that.offsets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, offsets);
    }

    @Override
    public String toString() {
        return "ConsumerGroupOffsetResetRequest{" +
                "groupName='" + groupName + '\'' +
                ", offsets=" + offsets +
                '}';
    }
}
