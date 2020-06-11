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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.TopicPartitionComparator;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupMemberInfo implements Comparable<ConsumerGroupMemberInfo> {

    private final String clientId;
    private final String memberId;
    private final String clientHost;
    private final Integer rebalanceTimeoutMs;
    private final Integer sessionTimeoutMs;
    private final LocalDateTime latestHeartbeatDate;
    private final List<TopicPartition> assignment;

    public ConsumerGroupMemberInfo(
            @JsonProperty("clientId") String clientId,
            @JsonProperty("memberId") String memberId,
            @JsonProperty("clientHost") String clientHost,
            @JsonProperty("rebalanceTimeoutMs") Integer rebalanceTimeoutMs,
            @JsonProperty("sessionTimeoutMs") Integer sessionTimeoutMs,
            @JsonProperty("latestHeartbeatDate") LocalDateTime latestHeartbeatDate,
            @JsonProperty("assignment") Collection<TopicPartition> assignment) {
        Validate.notBlank(clientId, "Client id is blank");
        if (assignment != null) {
            Validate.noNullElements(assignment, "Collection of assignments contains null elements");
        }

        this.clientId = clientId;
        this.memberId = memberId;
        this.clientHost = clientHost;
        this.rebalanceTimeoutMs = rebalanceTimeoutMs;
        this.sessionTimeoutMs = sessionTimeoutMs;
        this.latestHeartbeatDate = latestHeartbeatDate;
        this.assignment =
                !CollectionUtils.isEmpty(assignment) ?
                assignment.stream().
                        sorted(TopicPartitionComparator.INSTANCE).
                        collect(
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        Collections::unmodifiableList)) :
                Collections.emptyList();
    }

    public String getClientId() {
        return clientId;
    }
    public String getMemberId() {
        return memberId;
    }
    public String getClientHost() {
        return clientHost;
    }
    public Integer getRebalanceTimeoutMs() {
        return rebalanceTimeoutMs;
    }
    public Integer getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }
    public LocalDateTime getLatestHeartbeatDate() {
        return latestHeartbeatDate;
    }
    public List<TopicPartition> getAssignment() {
        return assignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clientId,
                memberId,
                clientHost,
                rebalanceTimeoutMs,
                sessionTimeoutMs,
                latestHeartbeatDate,
                assignment);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ConsumerGroupMemberInfo that = (ConsumerGroupMemberInfo)obj;
        return
                Objects.equals(this.clientId, that.clientId) &&
                Objects.equals(this.memberId, that.memberId) &&
                Objects.equals(this.clientHost, that.clientHost) &&
                Objects.equals(this.rebalanceTimeoutMs, that.rebalanceTimeoutMs) &&
                Objects.equals(this.sessionTimeoutMs, that.sessionTimeoutMs) &&
                Objects.equals(this.latestHeartbeatDate, that.latestHeartbeatDate) &&
                Objects.equals(this.assignment, that.assignment);
    }

    @Override
    public String toString() {
        return
                "{clientId: " + clientId +
                ", memberId: " + memberId +
                ", clientHost: " + clientHost +
                ", rebalanceTimeoutMs: " + rebalanceTimeoutMs +
                ", sessionTimeoutMs: " + sessionTimeoutMs +
                ", latestHeartbeatDate: " + latestHeartbeatDate +
                ", assignment: " + assignment +
                "}";
    }

    @Override
    public int compareTo(ConsumerGroupMemberInfo that) {
        int result = ObjectUtils.compare(this.clientId, that.clientId);
        if (result == 0) {
            result = ObjectUtils.compare(this.memberId, that.memberId);
        }
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String clientId;
        private String memberId;
        private String clientHost;
        private Integer rebalanceTimeoutMs;
        private Integer sessionTimeoutMs;
        private LocalDateTime latestHeartbeatDate;
        private Collection<TopicPartition> assignment;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }
        public Builder memberId(String memberId) {
            this.memberId = memberId;
            return this;
        }
        public Builder clientHost(String clientHost) {
            this.clientHost = clientHost;
            return this;
        }
        public Builder rebalanceTimeoutMs(Integer rebalanceTimeoutMs) {
            this.rebalanceTimeoutMs = rebalanceTimeoutMs;
            return this;
        }
        public Builder sessionTimeoutMs(Integer sessionTimeoutMs) {
            this.sessionTimeoutMs = sessionTimeoutMs;
            return this;
        }
        public Builder latestHeartbeatDate(Long latestHeartbeatDate) {
            if (latestHeartbeatDate == null) {
                return latestHeartbeatDate((LocalDateTime)null);
            } else {
                return latestHeartbeatDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(latestHeartbeatDate),
                                TimeZone.getDefault().toZoneId()));
            }
        }
        public Builder latestHeartbeatDate(LocalDateTime latestHeartbeatDate) {
            this.latestHeartbeatDate = latestHeartbeatDate;
            return this;
        }
        public Builder assignment(Collection<TopicPartition> assignment) {
            this.assignment = assignment;
            return this;
        }

        public ConsumerGroupMemberInfo build() {
            return new ConsumerGroupMemberInfo(
                    clientId,
                    memberId,
                    clientHost,
                    rebalanceTimeoutMs,
                    sessionTimeoutMs,
                    latestHeartbeatDate,
                    assignment);
        }

    }

}
