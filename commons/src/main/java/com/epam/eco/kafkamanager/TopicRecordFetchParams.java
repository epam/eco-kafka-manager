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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.OffsetRange;

import static java.util.Objects.isNull;

/**
 * @author Andrei_Tytsik
 */
public class TopicRecordFetchParams {

    public static final long MIN_LIMIT = 1;
    public static final long MAX_LIMIT = 100;

    private final DataFormat keyDataFormat;
    private final DataFormat valueDataFormat;
    private final Map<Integer, OffsetRange> offsets;
    private final long timeoutInMs;
    private final long limit;
    private final FetchMode fetchMode;
    private final long timestamp;
    private final boolean useCache;

    private final long cacheExpirationTimeMin;

    public TopicRecordFetchParams(
            @JsonProperty("keyDataFormat") DataFormat keyDataFormat,
            @JsonProperty("valueDataFormat") DataFormat valueDataFormat,
            @JsonProperty("offsets") Map<Integer, OffsetRange> offsets,
            @JsonProperty("limit") long limit,
            @JsonProperty("timeoutInMs") long timeoutInMs,
            @JsonProperty("fetchMode") FetchMode fetchMode,
            @JsonProperty("calculatedTimestamp") long timestamp,
            @JsonProperty("useCache") boolean useCache,
            @JsonProperty("cacheExpirationTimeMin") long cacheExpirationTimeMin
    ) {
        Validate.notNull(keyDataFormat, "Key data format can't be null");
        Validate.notNull(valueDataFormat, "Value data format can't be null");
        Validate.notNull(offsets, "Offsets can't be null");
        Validate.notEmpty(offsets, "Offsets can't be empty");
        Validate.isTrue(limit >= MIN_LIMIT && limit <= MAX_LIMIT,
                String.format("Limit is invalid, should be within range [%d..%d]", MIN_LIMIT, MAX_LIMIT));

        this.keyDataFormat = keyDataFormat;
        this.valueDataFormat = valueDataFormat;
        this.offsets = Collections.unmodifiableMap(offsets);
        this.limit = limit;
        this.timeoutInMs = timeoutInMs;
        this.fetchMode = isNull(fetchMode) ? FetchMode.FETCH_FORWARD : fetchMode;
        this.timestamp = timestamp;
        this.useCache = useCache;
        this.cacheExpirationTimeMin = cacheExpirationTimeMin;
    }

    public DataFormat getKeyDataFormat() {
        return keyDataFormat;
    }
    public DataFormat getValueDataFormat() {
        return valueDataFormat;
    }
    public Map<Integer, OffsetRange> getOffsets() {
        return offsets;
    }
    public long getLimit() {
        return limit;
    }
    public long getTimeoutInMs() {
        return timeoutInMs;
    }
    public FetchMode getFetchMode() {
        return fetchMode;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean getUseCache() {
        return useCache;
    }

    public Long getCacheExpirationTimeMin() {
        return cacheExpirationTimeMin;
    }

    public enum DataFormat {
        AVRO, STRING, JSON_STRING, HEX_STRING, PROTOCOL_BUFFERS
    }

}
