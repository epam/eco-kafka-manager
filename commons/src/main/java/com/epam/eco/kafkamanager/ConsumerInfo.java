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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerInfo implements Comparable<ConsumerInfo> {

    private final String groupName;
    private final String clientId;
    private final String memberId;
    private final String clientHost;
    private final String protocolType;
    private final Integer rebalanceTimeoutMs;
    private final Integer sessionTimeoutMs;
    private final LocalDateTime latestHeartbeatDate;

    public ConsumerInfo(
            @JsonProperty("groupName") String groupName,
            @JsonProperty("clientId") String clientId,
            @JsonProperty("memberId") String memberId,
            @JsonProperty("clientHost") String clientHost,
            @JsonProperty("protocolType") String protocolType,
            @JsonProperty("rebalanceTimeoutMs") Integer rebalanceTimeoutMs,
            @JsonProperty("sessionTimeoutMs") Integer sessionTimeoutMs,
            @JsonProperty("latestHeartbeatDate") LocalDateTime latestHeartbeatDate) {
        Validate.notBlank(groupName, "Group name is blank");
        Validate.notBlank(clientId, "Client id is blank");

        this.groupName = groupName;
        this.clientId = clientId;
        this.memberId = memberId;
        this.clientHost = clientHost;
        this.protocolType = protocolType;
        this.rebalanceTimeoutMs = rebalanceTimeoutMs;
        this.sessionTimeoutMs = sessionTimeoutMs;
        this.latestHeartbeatDate = latestHeartbeatDate;
    }

    public String getGroupName() {
        return groupName;
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
    public String getProtocolType() {
        return protocolType;
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

    @Override
    public int hashCode() {
        return Objects.hash(
                groupName,
                clientId,
                memberId,
                clientHost,
                protocolType,
                rebalanceTimeoutMs,
                sessionTimeoutMs,
                latestHeartbeatDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ConsumerInfo that = (ConsumerInfo)obj;
        return
                Objects.equals(this.groupName, that.groupName) &&
                Objects.equals(this.clientId, that.clientId) &&
                Objects.equals(this.memberId, that.memberId) &&
                Objects.equals(this.clientHost, that.clientHost) &&
                Objects.equals(this.protocolType, that.protocolType) &&
                Objects.equals(this.rebalanceTimeoutMs, that.rebalanceTimeoutMs) &&
                Objects.equals(this.sessionTimeoutMs, that.sessionTimeoutMs) &&
                Objects.equals(this.latestHeartbeatDate, that.latestHeartbeatDate);
    }

    @Override
    public String toString() {
        return
                "{groupName: " + groupName +
                ", clientId: " + clientId +
                ", memberId: " + memberId +
                ", clientHost: " + clientHost +
                ", protocolType: " + protocolType +
                ", rebalanceTimeoutMs: " + rebalanceTimeoutMs +
                ", sessionTimeoutMs: " + sessionTimeoutMs +
                ", latestHeartbeatDate: " + latestHeartbeatDate +
                "}";
    }

    @Override
    public int compareTo(ConsumerInfo that) {
        int result = ObjectUtils.compare(this.groupName, that.groupName);
        if (result == 0) {
            result = ObjectUtils.compare(this.clientId, that.clientId);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.memberId, that.memberId);
        }
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String groupName;
        private String clientId;
        private String memberId;
        private String clientHost;
        private String protocolType;
        private Integer rebalanceTimeoutMs;
        private Integer sessionTimeoutMs;
        private LocalDateTime latestHeartbeatDate;

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }
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
        public Builder protocolType(String protocolType) {
            this.protocolType = protocolType;
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
        public Builder latestHeartbeatDate(LocalDateTime latestHeartbeatDate) {
            this.latestHeartbeatDate = latestHeartbeatDate;
            return this;
        }
        public Builder latestHeartbeatDate(Long latestHeartbeatDate) {
            if (latestHeartbeatDate != null && latestHeartbeatDate > 0) {
                this.latestHeartbeatDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(latestHeartbeatDate),
                        TimeZone.getDefault().toZoneId());
            } else {
                this.latestHeartbeatDate = null;
            }
            return this;
        }

        public ConsumerInfo build() {
            return new ConsumerInfo(
                    groupName,
                    clientId,
                    memberId,
                    clientHost,
                    protocolType,
                    rebalanceTimeoutMs,
                    sessionTimeoutMs,
                    latestHeartbeatDate);
        }

    }

}
