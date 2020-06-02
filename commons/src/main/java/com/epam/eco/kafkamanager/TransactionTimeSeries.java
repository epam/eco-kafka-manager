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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.series.MergeFunction;
import com.epam.eco.commons.series.TimeSeries;

/**
 * @author Andrei_Tytsik
 */
public class TransactionTimeSeries extends TimeSeries<Integer> {

    private static final int MAX_SIZE = 3;
    private static final TemporalUnit GRANULARITY = ChronoUnit.MINUTES;
    private static final MergeFunction<Integer> MERGE_FUNCTION = MergeFunction.addInt();

    private final String transactionalId;

    public TransactionTimeSeries(String transactionalId) {
        this(true, transactionalId, null);
    }

    @JsonCreator
    private TransactionTimeSeries(
            @JsonProperty("transactionalId") String transactionalId,
            @JsonProperty("data") Map<LocalDateTime, Integer> data) {
        this(true, transactionalId, data);
    }

    private TransactionTimeSeries(
            boolean modifiable,
            String transactionalId,
            Map<LocalDateTime, Integer> data) {
        super(modifiable, MAX_SIZE, GRANULARITY, MERGE_FUNCTION, data);

        Validate.notNull(transactionalId, "Transactional Id is null");

        this.transactionalId = transactionalId;
    }

    @JsonIgnore
    @Override
    public TemporalUnit getGranularity() {
        return super.getGranularity();
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public Map<LocalDateTime, Integer> getData() {
        return toMap();
    }

    public Integer currentRatePerSec() {
        return currentRatePer(ChronoUnit.SECONDS);
    }

    public Integer currentRatePerMinute() {
        return currentRatePer(ChronoUnit.MINUTES);
    }

    public Integer currentRatePerHour() {
        return currentRatePer(ChronoUnit.HOURS);
    }

    public Integer currentRatePer(TemporalUnit unit) {
        Validate.notNull(unit, "Temporal unit is null");

        LocalDateTime now = LocalDateTime.now();

        Integer currentCount = value(now);
        if (currentCount == null) {
            return null;
        }

        int count = 0;
        Duration duration = null;

        Integer previousCount = previousSerialValue(now);
        if (previousCount != null) {
            count = previousCount + currentCount;
            duration = Duration.between(previousSerialKey(now), now);
        } else {
            count = currentCount;
            duration = Duration.between(key(now), now);
        }

        long durationSec = duration.getSeconds();
        if (durationSec == 0) {
            return NumberUtils.INTEGER_ZERO;
        }

        long unitSec = unit.getDuration().getSeconds();
        return (int)Math.round((double)unitSec * count / durationSec);
    }

    public Integer currentDelta() {
        return deltaAtDate(LocalDateTime.now());
    }

    public Integer deltaAtDate(LocalDateTime dateTime) {
        Integer count = value(dateTime);
        Integer previousCount = previousValue(dateTime);
        return
                count != null && previousCount != null ?
                count - previousCount :
                null;
    }

    @Override
    public TransactionTimeSeries copy() {
        return new TransactionTimeSeries(true, transactionalId, getData());
    }

    @Override
    public TransactionTimeSeries unmodifiableCopy() {
        return new TransactionTimeSeries(false, transactionalId, getData());
    }

    @Override
    public String toString() {
        return
                "{transactionalId: " + transactionalId +
                ", data: " + getData() +
                "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionalId, getData());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TransactionTimeSeries that = (TransactionTimeSeries)obj;
        return
                Objects.equals(this.transactionalId, that.transactionalId) &&
                Objects.equals(this.getData(), that.getData());
    }

    public static TransactionTimeSeries empty(String transactionalId) {
        return new TransactionTimeSeries(transactionalId);
    }

    public static TransactionTimeSeries unmodifiableEmpty(String transactionalId) {
        return new TransactionTimeSeries(false, transactionalId, Collections.emptyMap());
    }

}
