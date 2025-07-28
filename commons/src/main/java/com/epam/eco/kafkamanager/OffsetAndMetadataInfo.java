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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import com.epam.eco.commons.kafka.TopicPartitionComparator;

/**
 * @author Andrei_Tytsik
 */
@JsonDeserialize(builder = OffsetAndMetadataInfo.Builder.class)
public class OffsetAndMetadataInfo implements Comparable<OffsetAndMetadataInfo> {

    private final TopicPartition topicPartition;
    private final long offset;
    private final String metadata;
    private final LocalDateTime commitDate;
    private final LocalDateTime expireDate;

    public OffsetAndMetadataInfo(
            TopicPartition topicPartition,
            long offset,
            String metadata,
            LocalDateTime commitDate,
            LocalDateTime expireDate) {
        Validate.notNull(topicPartition, "TopicPartition is null");

        this.topicPartition = topicPartition;
        this.offset = offset;
        this.metadata = metadata;
        this.commitDate = commitDate;
        this.expireDate = expireDate;
    }

    public String getTopic() {
        return topicPartition.topic();
    }
    public int getPartition() {
        return topicPartition.partition();
    }
    @JsonIgnore
    public TopicPartition getTopicPartition() {
        return topicPartition;
    }
    public long getOffset() {
        return offset;
    }
    public String getMetadata() {
        return metadata;
    }
    public LocalDateTime getCommitDate() {
        return commitDate;
    }
    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    @JsonIgnore
    public boolean isActualForSecond() {
        return isActualFor(ChronoUnit.SECONDS);
    }

    @JsonIgnore
    public boolean isActualForMinute() {
        return isActualFor(ChronoUnit.MINUTES);
    }

    @JsonIgnore
    public boolean isActualForHour() {
        return isActualFor(ChronoUnit.HOURS);
    }

    public boolean isActualFor(TemporalUnit temporalUnit) {
        Validate.notNull(temporalUnit, "Temporal unit is null");

        if (commitDate == null) {
            return false;
        }

        Duration sinceCommitDate = Duration.between(commitDate, LocalDateTime.now());
        return temporalUnit.getDuration().compareTo(sinceCommitDate) >= 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicPartition, offset, metadata, commitDate, expireDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        OffsetAndMetadataInfo that = (OffsetAndMetadataInfo)obj;
        return
                Objects.equals(this.topicPartition, that.topicPartition) &&
                        Objects.equals(this.offset, that.offset) &&
                        Objects.equals(this.metadata, that.metadata) &&
                        Objects.equals(this.commitDate, that.commitDate) &&
                        Objects.equals(this.expireDate, that.expireDate);
    }

    @Override
    public String toString() {
        return
                "{topicPartition: " + topicPartition +
                        ", offset: " + offset +
                        ", metadata: " + metadata +
                        ", commitDate: " + commitDate +
                        ", expireDate: " + expireDate +
                        "}";
    }

    @Override
    public int compareTo(OffsetAndMetadataInfo that) {
        return TopicPartitionComparator.INSTANCE.compare(
                this.topicPartition,
                that.topicPartition);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = StringUtils.EMPTY)
    public static class Builder {

        private TopicPartition topicPartition;
        private String topic;
        private int partition;
        private long offset;
        private String metadata;
        private LocalDateTime commitDate;
        private LocalDateTime expireDate;

        public Builder topicPartition(TopicPartition topicPartition) {
            this.topicPartition = topicPartition;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder partition(int partition) {
            this.partition = partition;
            return this;
        }

        public Builder offset(long offset) {
            this.offset = offset >= 0 ? offset : 0;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder commitDate(Long commitDate) {
            if (commitDate == null) {
                return commitDate((LocalDateTime)null);
            } else {
                return commitDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(commitDate),
                                TimeZone.getDefault().toZoneId()));
            }
        }

        @JsonFormat
        public Builder commitDate(LocalDateTime commitDate) {
            this.commitDate = commitDate;
            return this;
        }

        public Builder expireDate(Long expireDate) {
            if (expireDate == null) {
                return expireDate((LocalDateTime)null);
            } else {
                return expireDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(expireDate),
                                TimeZone.getDefault().toZoneId()));
            }
        }

        @JsonFormat
        public Builder expireDate(LocalDateTime expireDate) {
            this.expireDate = expireDate;
            return this;
        }

        private TopicPartition buildTopicPartition() {
            if (topicPartition != null) {
                return topicPartition;
            } else if (topic != null && partition >= 0) {
                return new TopicPartition(topic, partition);
            }
            return null;
        }

        public OffsetAndMetadataInfo build() {
            return new OffsetAndMetadataInfo(
                    buildTopicPartition(),
                    offset,
                    metadata,
                    commitDate,
                    expireDate);
        }

    }

}
