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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class TransactionSearchCriteria implements SearchCriteria<TransactionInfo> {

    private final String transactionalId;
    private final TransactionState state;
    private final String topicName;

    public TransactionSearchCriteria(
            @JsonProperty("transactionalId") String transactionalId,
            @JsonProperty("state") TransactionState state,
            @JsonProperty("topicName") String topicName) {
        this.transactionalId = transactionalId;
        this.state = state;
        this.topicName = topicName;
    }

    public String getTransactionalId() {
        return transactionalId;
    }
    public TransactionState getState() {
        return state;
    }
    public String getTopicName() {
        return topicName;
    }

    @Override
    public boolean matches(TransactionInfo obj) {
        Validate.notNull(obj, "TransactionInfo is null");

        return
                (
                        StringUtils.isBlank(transactionalId) ||
                        StringUtils.containsIgnoreCase(obj.getTransactionalId(), transactionalId)) &&
                (state == null || Objects.equals(obj.getCurrentMetadata().getState(), state)) &&
                (
                        StringUtils.isBlank(topicName) ||
                        obj.getTopicNames().stream().anyMatch(elem -> StringUtils.containsIgnoreCase(elem, topicName)));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TransactionSearchCriteria that = (TransactionSearchCriteria) obj;
        return
                Objects.equals(this.transactionalId, that.transactionalId) &&
                Objects.equals(this.state, that.state) &&
                Objects.equals(this.topicName, that.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionalId, state, topicName);
    }

    @Override
    public String toString() {
        return
                "{transactionalId: " + transactionalId +
                ", state: " + state +
                ", topicName: " + topicName +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(TransactionSearchCriteria origin) {
        return new Builder(origin);
    }

    public static TransactionSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TransactionSearchCriteria.class);
    }

    public static TransactionSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TransactionSearchCriteria.class);
    }

    public static class Builder {

        private String transactionalId;
        private TransactionState state;
        private String topicName;

        private Builder(TransactionSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.transactionalId = origin.transactionalId;
            this.state = origin.state;
            this.topicName = origin.topicName;
        }

        public Builder transactionalId(String transactionalId) {
            this.transactionalId = transactionalId;
            return this;
        }
        public Builder state(TransactionState state) {
            this.state = state;
            return this;
        }
        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public TransactionSearchCriteria build() {
            return new TransactionSearchCriteria(transactionalId, state, topicName);
        }

    }

}
