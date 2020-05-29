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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopicRecordFetchByTimestampsParams extends TopicRecordFetchParams {

    private final Map<Integer, Long> partitionTimestamps;

    public TopicRecordFetchByTimestampsParams(
            @JsonProperty("keyDataFormat") DataFormat keyDataFormat,
            @JsonProperty("valueDataFormat") DataFormat valueDataFormat,
            @JsonProperty("limit") Long limit,
            @JsonProperty("timeoutInMs") Long timeoutInMs,
            @JsonProperty("partitionTimestamps") Map<Integer, Long> partitionTimestamps) {
        super(Type.BY_TIMESTAMPS, keyDataFormat, valueDataFormat, limit, timeoutInMs);

        Validate.notNull(partitionTimestamps, "Timestamps can't be null");
        Validate.notEmpty(partitionTimestamps, "Timestamps can't be empty");
        this.partitionTimestamps = partitionTimestamps;
    }

    public Map<Integer, Long> getPartitionTimestamps() {
        return partitionTimestamps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TopicRecordFetchByTimestampsParams that = (TopicRecordFetchByTimestampsParams) o;
        return Objects.equals(partitionTimestamps, that.partitionTimestamps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), partitionTimestamps);
    }

    @Override
    public String toString() {
        return "TopicRecordFetchByTimestampsParams{" +
                "partitionTimestamps=" + partitionTimestamps +
                ", type=" + type +
                '}';
    }
}
