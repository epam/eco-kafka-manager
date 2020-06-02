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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Andrei_Tytsik
 */
public class TransactionInfo implements Comparable<TransactionInfo> {

    private final List<TransactionMetadataInfo> history;
    private final Set<String> topicNames;
    private final TransactionTimeSeries successTimeSeries;
    private final TransactionTimeSeries failTimeSeries;
    private final Statistics execTimeStats;

    public TransactionInfo(
            @JsonProperty("history") List<TransactionMetadataInfo> history,
            @JsonProperty("topicNames") Set<String> topicNames,
            @JsonProperty("successTimeSeries") TransactionTimeSeries successTimeSeries,
            @JsonProperty("failTimeSeries") TransactionTimeSeries failTimeSeries,
            @JsonProperty("execTimeStats") Statistics execTimeStats) {
        Validate.notEmpty(history, "History is null or empty");
        Validate.noNullElements(history, "History contains null elements");
        Validate.notNull(topicNames, "Collection of topics is null");
        Validate.noNullElements(topicNames, "Collection of topics contains null elements");
        Validate.notNull(successTimeSeries, "Success TimeSeries is null");
        Validate.notNull(failTimeSeries, "Fail TimeSeries is null");
        Validate.notNull(execTimeStats, "Execution time stats is null");

        this.history = Collections.unmodifiableList(new ArrayList<>(history));
        this.topicNames = Collections.unmodifiableSet(new TreeSet<>(topicNames));
        this.successTimeSeries = successTimeSeries.unmodifiableCopy();
        this.failTimeSeries = failTimeSeries.unmodifiableCopy();
        this.execTimeStats = execTimeStats;
    }

    public List<TransactionMetadataInfo> getHistory() {
        return history;
    }
    @JsonIgnore
    public String getTransactionalId() {
        return getCurrentMetadata().getTransactionalId();
    }
    @JsonIgnore
    public TransactionMetadataInfo getCurrentMetadata() {
        return history.get(history.size() - 1);
    }
    public Set<String> getTopicNames() {
        return topicNames;
    }
    public TransactionTimeSeries getSuccessTimeSeries() {
        return successTimeSeries;
    }
    public TransactionTimeSeries getFailTimeSeries() {
        return failTimeSeries;
    }
    public Statistics getExecTimeStats() {
        return execTimeStats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                history,
                topicNames,
                successTimeSeries,
                failTimeSeries,
                execTimeStats);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TransactionInfo that = (TransactionInfo)obj;
        return
                Objects.equals(this.history, that.history) &&
                Objects.equals(this.topicNames, that.topicNames) &&
                Objects.equals(this.successTimeSeries, that.successTimeSeries) &&
                Objects.equals(this.failTimeSeries, that.failTimeSeries) &&
                Objects.equals(this.execTimeStats, that.execTimeStats);
    }

    @Override
    public String toString() {
        return
                "{history: " + history +
                ", topicNames: " + topicNames +
                ", successTimeSeries: " + successTimeSeries +
                ", failTimeSeries: " + failTimeSeries +
                ", execTimeStats: " + execTimeStats +
                "}";
    }

    @Override
    public int compareTo(TransactionInfo that) {
        int result = ObjectUtils.compare(
                this.getCurrentMetadata().getTransactionalId(),
                that.getCurrentMetadata().getTransactionalId());
        if (result == 0) {
            result = ObjectUtils.compare(
                    this.getCurrentMetadata().getProducerId(),
                    that.getCurrentMetadata().getProducerId());
        }
        if (result == 0) {
            result = ObjectUtils.compare(
                    this.getCurrentMetadata().getProducerEpoch(),
                    that.getCurrentMetadata().getProducerEpoch());
        }
        if (result == 0) {
            result = ObjectUtils.compare(
                    this.getCurrentMetadata().getState(),
                    that.getCurrentMetadata().getState());
        }
        return result;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<TransactionMetadataInfo> history = new ArrayList<>();
        private Set<String> topicNames = new HashSet<>();
        private TransactionTimeSeries successTimeSeries;
        private TransactionTimeSeries failTimeSeries;
        private Statistics execTimeStats;

        public Builder() {
            this(null);
        }

        public Builder(TransactionInfo origin) {
            if (origin == null) {
                return;
            }

            this.history.addAll(origin.getHistory());
            this.topicNames.addAll(origin.getTopicNames());
            this.successTimeSeries = origin.successTimeSeries;
            this.failTimeSeries = origin.failTimeSeries;
            this.execTimeStats = origin.execTimeStats;
        }

        public Builder history(List<TransactionMetadataInfo> history) {
            this.history.clear();
            if (history != null) {
                this.history.addAll(history);
            }
            return this;
        }
        public Builder metadata(TransactionMetadataInfo metadata) {
            this.history.add(metadata);
            return this;
        }
        public Builder topicNames(Set<String> topicNames) {
            this.topicNames.clear();
            if (topicNames != null) {
                this.topicNames.addAll(topicNames);
            }
            return this;
        }
        public Builder successTimeSeries(TransactionTimeSeries successTimeSeries) {
            this.successTimeSeries = successTimeSeries;
            return this;
        }
        public Builder failTimeSeries(TransactionTimeSeries failTimeSeries) {
            this.failTimeSeries = failTimeSeries;
            return this;
        }
        public Builder execTimeStats(Statistics execTimeStats) {
            this.execTimeStats = execTimeStats;
            return this;
        }

        public TransactionInfo build() {
            return new TransactionInfo(
                    history,
                    topicNames,
                    successTimeSeries,
                    failTimeSeries,
                    execTimeStats);
        }

    }

}
