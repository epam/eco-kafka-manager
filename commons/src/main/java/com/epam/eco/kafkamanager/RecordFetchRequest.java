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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class RecordFetchRequest {

    public static final long MIN_LIMIT = 1;
    public static final long MAX_LIMIT = 100;

    private final DataFormat keyDataFormat;
    private final DataFormat valueDataFormat;
    private final Map<Integer, Long> offsets;
    private final Map<Integer, Long> partitionTimestamps;
    private final Long timeoutInMs;
    private final Long limit;
    private final boolean fetchByTimestamp;

    public RecordFetchRequest(
            @JsonProperty("keyDataFormat") DataFormat keyDataFormat,
            @JsonProperty("valueDataFormat") DataFormat valueDataFormat,
            @JsonProperty("offsets") Map<Integer, Long> offsets,
            @JsonProperty("partitionTimestamps") Map<Integer, Long> partitionTimestamps,
            @JsonProperty("limit") Long limit,
            @JsonProperty("timeoutInMs") Long timeoutInMs,
            @JsonProperty("fetchByTimestamp") boolean fetchByTimestamp) {
        Validate.notNull(keyDataFormat, "Key data format can't be null");
        Validate.notNull(valueDataFormat, "Value data format can't be null");
        Validate.notNull(offsets, "Offsets can't be null");
        Validate.isTrue(MapUtils.isNotEmpty(offsets) || MapUtils.isNotEmpty(partitionTimestamps),
                "Offsets/Timestamps cannot be empty");
        Validate.isTrue(limit >= MIN_LIMIT && limit <= MAX_LIMIT,
                String.format("Limit is invalid, should be within range [%d..%d]", MIN_LIMIT, MAX_LIMIT));

        this.keyDataFormat = keyDataFormat;
        this.valueDataFormat = valueDataFormat;
        this.offsets = Collections.unmodifiableMap(offsets);
        this.partitionTimestamps = Collections.unmodifiableMap(partitionTimestamps);
        this.limit = limit;
        this.timeoutInMs = timeoutInMs;
        this.fetchByTimestamp = fetchByTimestamp;
    }

    public DataFormat getKeyDataFormat() {
        return keyDataFormat;
    }
    public DataFormat getValueDataFormat() {
        return valueDataFormat;
    }
    public Map<Integer, Long> getOffsets() {
        return offsets;
    }
    public Map<Integer, Long> getPartitionTimestamps() {
        return partitionTimestamps;
    }
    public Long getLimit() {
        return limit;
    }
    public Long getTimeoutInMs() {
        return timeoutInMs;
    }

    public boolean getFetchByTimestamp() {
        return fetchByTimestamp;
    }

    public enum DataFormat {
        AVRO, STRING, JSON_STRING, HEX_STRING
    }

}
