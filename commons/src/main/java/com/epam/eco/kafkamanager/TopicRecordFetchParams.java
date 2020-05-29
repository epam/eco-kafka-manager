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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TopicRecordFetchByOffsetsParam.class, name = "BY_OFFSETS"),
        @JsonSubTypes.Type(value = TopicRecordFetchByTimestampsParams.class, name = "BY_TIMESTAMPS"),
})
public abstract class TopicRecordFetchParams {

    public static final long MIN_LIMIT = 1;
    public static final long MAX_LIMIT = 100;

    private final DataFormat keyDataFormat;
    private final DataFormat valueDataFormat;
    private final Long timeoutInMs;
    private final Long limit;

    protected final Type type;

    public TopicRecordFetchParams(
            Type type,
            DataFormat keyDataFormat,
            DataFormat valueDataFormat,
            Long limit,
            Long timeoutInMs) {
        Validate.notNull(type, "Type can't be null");
        Validate.notNull(keyDataFormat, "Key data format can't be null");
        Validate.notNull(valueDataFormat, "Value data format can't be null");
        Validate.isTrue(limit >= MIN_LIMIT && limit <= MAX_LIMIT,
                String.format("Limit is invalid, should be within range [%d..%d]", MIN_LIMIT, MAX_LIMIT));

        this.type = type;
        this.keyDataFormat = keyDataFormat;
        this.valueDataFormat = valueDataFormat;
        this.timeoutInMs = timeoutInMs;
        this.limit = limit;
    }


    public static TopicRecordFetchByOffsetsParam byOffsets(DataFormat keyDataFormat,
                                                           DataFormat valueDataFormat,
                                                           Long limit,
                                                           Long timeoutInMs,
                                                           Map<Integer, Long> offsets) {
        return new TopicRecordFetchByOffsetsParam(keyDataFormat,
                valueDataFormat,
                limit,
                timeoutInMs,
                offsets);
    }

    public static TopicRecordFetchByTimestampsParams byTimestamps(DataFormat keyDataFormat,
                                                                  DataFormat valueDataFormat,
                                                                  Long limit,
                                                                  Long timeoutInMs,
                                                                  Map<Integer, Long> partitionTimestamps) {
        return new TopicRecordFetchByTimestampsParams(keyDataFormat,
                valueDataFormat,
                limit,
                timeoutInMs,
                partitionTimestamps);
    }

    public Type getType() {
        return type;
    }

    public DataFormat getKeyDataFormat() {
        return keyDataFormat;
    }

    public DataFormat getValueDataFormat() {
        return valueDataFormat;
    }

    public Long getTimeoutInMs() {
        return timeoutInMs;
    }

    public Long getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicRecordFetchParams that = (TopicRecordFetchParams) o;
        return keyDataFormat == that.keyDataFormat &&
                valueDataFormat == that.valueDataFormat &&
                Objects.equals(timeoutInMs, that.timeoutInMs) &&
                Objects.equals(limit, that.limit) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyDataFormat, valueDataFormat, timeoutInMs, limit, type);
    }

    protected enum Type {
        BY_OFFSETS, BY_TIMESTAMPS
    }

    public enum DataFormat {
        AVRO, STRING, JSON_STRING, HEX_STRING
    }
}
