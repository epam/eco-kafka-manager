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
package com.epam.eco.kafkamanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.series.MergeFunction;
import com.epam.eco.commons.series.TimeSeries;

/**
 * @author Andrei_Tytsik
 */
public class OffsetTimeSeries extends TimeSeries<Long> {

    private static final int MAX_SIZE = 10;
    private static final TemporalUnit GRANULARITY = ChronoUnit.MINUTES;
    private static final MergeFunction<Long> MERGE_FUNCTION = MergeFunction.replace();

    private final TopicPartition topicPartition;

    public OffsetTimeSeries(TopicPartition topicPartition) {
        this(true, topicPartition, null);
    }

    @JsonCreator
    private OffsetTimeSeries(
            @JsonProperty("topic") String topic,
            @JsonProperty("partition") int partition,
            @JsonProperty("data") Map<LocalDateTime, Long> data) {
        this(
                true,
                topic != null && partition >= 0 ? new TopicPartition(topic, partition) : null,
                data);
    }

    private OffsetTimeSeries(
            boolean modifiable,
            TopicPartition topicPartition,
            Map<LocalDateTime, Long> data) {
        super(modifiable, MAX_SIZE, GRANULARITY, MERGE_FUNCTION, data);

        Validate.notNull(topicPartition, "TopicPartition is null");

        this.topicPartition = topicPartition;
    }

    @JsonIgnore
    @Override
    public TemporalUnit getGranularity() {
        return super.getGranularity();
    }
    @JsonIgnore
    public TopicPartition getTopicPartition() {
        return topicPartition;
    }
    public String getTopic() {
        return topicPartition.topic();
    }
    public int getPartition() {
        return topicPartition.partition();
    }
    public Map<LocalDateTime, Long> getData() {
        return toMap();
    }

    public Long currentRatePerSec() {
        return currentRatePer(ChronoUnit.SECONDS);
    }

    public Long currentRatePerMinute() {
        return currentRatePer(ChronoUnit.MINUTES);
    }

    public Long currentRatePerHour() {
        return currentRatePer(ChronoUnit.HOURS);
    }

    public Long currentRatePer(TemporalUnit unit) {
        Validate.notNull(unit, "Temporal unit is null");

        LocalDateTime now = LocalDateTime.now();

        Long currentOffset = value(now);
        Long previousOffset = previousValue(now);
        if (currentOffset == null || previousOffset == null) {
            return null;
        }

        LocalDateTime previousKey = previousKey(now);

        Duration duration = Duration.between(
                previousKey.plus(1, getGranularity()),
                now);
        long durationSec = duration.getSeconds();
        if (durationSec == 0) {
            return NumberUtils.LONG_ZERO;
        }

        long delta = currentOffset - previousOffset;
        long unitSec = unit.getDuration().getSeconds();
        return Math.round((double)unitSec * delta / durationSec);
    }

    public Long currentDelta() {
        return deltaAtDate(LocalDateTime.now());
    }

    public Long deltaAtDate(LocalDateTime dateTime) {
        Long offset = value(dateTime);
        Long previousOffset = previousValue(dateTime);
        return
                offset != null && previousOffset != null ?
                offset - previousOffset :
                null;
    }

    @Override
    public OffsetTimeSeries copy() {
        return new OffsetTimeSeries(true, topicPartition, getData());
    }

    @Override
    public OffsetTimeSeries unmodifiableCopy() {
        return new OffsetTimeSeries(false, topicPartition, getData());
    }

    @Override
    public String toString() {
        return
                "{topicPartition: " + topicPartition +
                ", data: " + getData() +
                "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicPartition, getData());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        OffsetTimeSeries that = (OffsetTimeSeries)obj;
        return
                Objects.equals(this.topicPartition, that.topicPartition) &&
                Objects.equals(this.getData(), that.getData());
    }

}
