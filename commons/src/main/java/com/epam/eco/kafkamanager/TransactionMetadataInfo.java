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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.TransactionState;

/**
 * @author Andrei_Tytsik
 */
public class TransactionMetadataInfo {

    private final String transactionalId;
    private final long producerId;
    private final short producerEpoch;
    private final int timeoutMs;
    private final TransactionState state;
    private final List<TopicPartition> partitions;
    private final LocalDateTime startDate;
    private final LocalDateTime lastUpdateDate;

    public TransactionMetadataInfo(
            @JsonProperty("transactionalId") String transactionalId,
            @JsonProperty("producerId") long producerId,
            @JsonProperty("producerEpoch") short producerEpoch,
            @JsonProperty("timeoutMs") int timeoutMs,
            @JsonProperty("state") TransactionState state,
            @JsonProperty("partitions") List<TopicPartition> partitions,
            @JsonProperty("startDate") LocalDateTime startDate,
            @JsonProperty("lastUpdateDate") LocalDateTime lastUpdateDate) {
        Validate.notNull(transactionalId, "Transactional Id is null");
        Validate.isTrue(producerId >= 0, "Producer Id is invalid");
        Validate.isTrue(producerEpoch >= 0, "Producer Epoch is invalid");
        Validate.isTrue(timeoutMs >= 0, "Timeout is invalid");
        Validate.notNull(state, "State is null");
        Validate.notNull(partitions, "Collection of partitions is null");
        Validate.noNullElements(partitions, "Collection of partitions contains null elements");

        this.transactionalId = transactionalId;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
        this.timeoutMs = timeoutMs;
        this.state = state;
        this.partitions = Collections.unmodifiableList(new ArrayList<>(partitions));
        this.startDate = startDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getTransactionalId() {
        return transactionalId;
    }
    public long getProducerId() {
        return producerId;
    }
    public short getProducerEpoch() {
        return producerEpoch;
    }
    public int getTimeoutMs() {
        return timeoutMs;
    }
    public TransactionState getState() {
        return state;
    }
    public List<TopicPartition> getPartitions() {
        return partitions;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                transactionalId,
                producerId,
                producerEpoch,
                timeoutMs,
                state,
                partitions,
                startDate,
                lastUpdateDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TransactionMetadataInfo that = (TransactionMetadataInfo)obj;
        return
                Objects.equals(this.transactionalId, that.transactionalId) &&
                Objects.equals(this.producerId, that.producerId) &&
                Objects.equals(this.producerEpoch, that.producerEpoch) &&
                Objects.equals(this.timeoutMs, that.timeoutMs) &&
                Objects.equals(this.state, that.state) &&
                Objects.equals(this.partitions, that.partitions) &&
                Objects.equals(this.startDate, that.startDate) &&
                Objects.equals(this.lastUpdateDate, that.lastUpdateDate);
    }

    @Override
    public String toString() {
        return
                "{transactionalId: " + transactionalId +
                ", producerId: " + producerId +
                ", producerEpoch: " + producerEpoch +
                ", timeoutMs: " + timeoutMs +
                ", state: " + state +
                ", partitions: " + partitions +
                ", startDate: " + startDate +
                ", lastUpdateDate: " + lastUpdateDate +
                "}";
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String transactionalId;
        private long producerId;
        private short producerEpoch;
        private int timeoutMs;
        private TransactionState state;
        private final List<TopicPartition> partitions = new ArrayList<>();
        private LocalDateTime startDate;
        private LocalDateTime lastUpdateDate;

        public Builder() {
            this(null);
        }

        public Builder(TransactionMetadataInfo origin) {
            if (origin == null) {
                return;
            }

            this.transactionalId = origin.transactionalId;
            this.producerId = origin.producerId;
            this.producerEpoch = origin.producerEpoch;
            this.timeoutMs = origin.timeoutMs;
            this.state = origin.state;
            this.partitions.addAll(origin.partitions);
            this.startDate = origin.startDate;
            this.lastUpdateDate = origin.lastUpdateDate;
        }

        public Builder transactionalId(String transactionalId) {
            this.transactionalId = transactionalId;
            return this;
        }
        public Builder producerId(long producerId) {
            this.producerId = producerId;
            return this;
        }
        public Builder producerEpoch(short producerEpoch) {
            this.producerEpoch = producerEpoch;
            return this;
        }
        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
        public Builder state(TransactionState state) {
            this.state = state;
            return this;
        }
        public Builder state(kafka.coordinator.transaction.TransactionState state) {
            this.state = TransactionState.fromScala(state);
            return this;
        }
        public Builder partitions(List<TopicPartition> partitions) {
            this.partitions.clear();
            if (partitions != null) {
                this.partitions.addAll(partitions);
            }
            return this;
        }
        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }
        public Builder startDate(Long startDate) {
            if (startDate != null && startDate > 0) {
                this.startDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(startDate),
                        TimeZone.getDefault().toZoneId());
            } else {
                this.startDate = null;
            }
            return this;
        }
        public Builder lastUpdateDate(LocalDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }
        public Builder lastUpdateDate(Long lastUpdateDate) {
            if (lastUpdateDate != null && lastUpdateDate > 0) {
                this.lastUpdateDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastUpdateDate),
                        TimeZone.getDefault().toZoneId());
            } else {
                this.lastUpdateDate = null;
            }
            return this;
        }

        public TransactionMetadataInfo build() {
            return new TransactionMetadataInfo(
                    transactionalId,
                    producerId,
                    producerEpoch,
                    timeoutMs,
                    state,
                    partitions,
                    startDate,
                    lastUpdateDate);
        }

    }

}
