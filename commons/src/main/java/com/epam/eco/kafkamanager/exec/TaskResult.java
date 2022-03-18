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
package com.epam.eco.kafkamanager.exec;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Andrei_Tytsik
 */
public class TaskResult<R> {

    private final R value;
    private final Date startedAt;
    private final Date finishedAt;
    private final RuntimeException error;

    public TaskResult(
            @JsonProperty("value") R value,
            @JsonProperty("startedAt") Date startedAt,
            @JsonProperty("finishedAt") Date finishedAt,
            @JsonProperty("error") RuntimeException error) {
        Validate.notNull(startedAt, "Start date is null");
        Validate.notNull(finishedAt, "Finish date is null");
        Validate.isTrue(finishedAt.compareTo(startedAt) >= 0, "Start date > finish date");

        this.value = value;
        this.startedAt = (Date) startedAt.clone();
        this.finishedAt = (Date) finishedAt.clone();
        this.error = error;
    }

    public Date getStartedAt() {
        return (Date) startedAt.clone();
    }
    public Date getFinishedAt() {
        return (Date) finishedAt.clone();
    }
    @JsonIgnore
    public long getElapsed() {
        return finishedAt.getTime() - startedAt.getTime();
    }
    @JsonIgnore
    public String getElapsedFormattedAsHMS() {
        return DurationFormatUtils.formatDurationHMS(getElapsed());
    }
    public RuntimeException getError() {
        return error;
    }
    @JsonIgnore
    public boolean isSuccessful() {
        return error == null;
    }
    public R getValue() {
        if (error != null) {
            throw error;
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TaskResult<?> that = (TaskResult<?>) obj;
        return
                Objects.equals(value, that.value) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(finishedAt, that.finishedAt) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, startedAt, finishedAt, error);
    }

    @Override
    public String toString() {
        return
                "{value: " + value +
                ", startedAt: " + startedAt +
                ", finishedAt: " + finishedAt +
                ", error: " + error +
                "}";
    }

    public static <R> TaskResult<R> of(Supplier<R> supplier) {
        TaskResult.Builder<R> builder = TaskResult.builder();
        try {
            builder.startedAtNow();
            builder.value(supplier.get());
        } catch (RuntimeException re) {
            builder.error(re);
        } finally {
            builder.finishedAtNow();
        }
        return builder.build();
    }

    public static <R> Builder<R> builder() {
        return new Builder<>();
    }

    public static class Builder<R> {

        private R value;
        private Date startedAt;
        private Date finishedAt;
        private RuntimeException error;

        public Builder<R> value(R value) {
            this.value = value;
            return this;
        }
        public Builder<R> startedAt(Date startedAt) {
            this.startedAt = startedAt != null ? (Date) startedAt.clone() : null;
            return this;
        }
        public Builder<R> finishedAt(Date finishedAt) {
            this.finishedAt = finishedAt != null ? (Date) finishedAt.clone() : null;
            return this;
        }
        public Builder<R> startedAtNow() {
            startedAt = new Date();
            return this;
        }
        public Builder<R> finishedAtNow() {
            finishedAt = new Date();
            return this;
        }
        public Builder<R> startedAndFinishedAtNow() {
            startedAt = new Date();
            finishedAt = startedAt;
            return this;
        }
        public Builder<R> error(RuntimeException error) {
            this.error = error;
            return this;
        }
        public TaskResult<R> build() {
            return new TaskResult<>(value, startedAt, finishedAt, error);
        }

    }

}
