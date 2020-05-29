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

public class TopicRecordFetchByOffsetsParam extends TopicRecordFetchParams {

    private final Map<Integer, Long> offsets;

    public TopicRecordFetchByOffsetsParam(
            @JsonProperty("keyDataFormat") DataFormat keyDataFormat,
            @JsonProperty("valueDataFormat") DataFormat valueDataFormat,
            @JsonProperty("limit") Long limit,
            @JsonProperty("timeoutInMs") Long timeoutInMs,
            @JsonProperty("offsets") Map<Integer, Long> offsets) {
        super(Type.BY_OFFSETS, keyDataFormat, valueDataFormat, limit, timeoutInMs);

        Validate.notNull(offsets, "Offsets can't be null");
        Validate.notEmpty(offsets, "Offsets can't be empty");
        this.offsets = offsets;
    }

    public Map<Integer, Long> getOffsets() {
        return offsets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TopicRecordFetchByOffsetsParam that = (TopicRecordFetchByOffsetsParam) o;
        return Objects.equals(offsets, that.offsets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), offsets);
    }

    @Override
    public String toString() {
        return "TopicRecordFetchByOffsetsParam{" +
                "offsets=" + offsets +
                '}';
    }
}
