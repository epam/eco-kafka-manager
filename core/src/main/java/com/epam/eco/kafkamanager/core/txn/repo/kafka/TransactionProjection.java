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
package com.epam.eco.kafkamanager.core.txn.repo.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.commons.kafka.TransactionState;

import kafka.coordinator.transaction.TransactionMetadata;

/**
 * @author Andrei_Tytsik
 */
class TransactionProjection {

    private static final int HISTORY_SIZE = 16;

    private final List<TransactionMetadata> history;
    private final Set<String> topics;
    private final int commitCount;
    private final int abortCount;
    private final SummaryStatistics execTimeStats;

    private TransactionProjection(
            Collection<TransactionMetadata> history,
            int commitCount,
            int abortCount,
            SummaryStatistics execTimeStats) {
        Validate.notEmpty(history, "History is null or empty");
        Validate.noNullElements(history, "History contains null elements");
        Validate.isTrue(commitCount >= 0, "Commit count is invalid");
        Validate.isTrue(abortCount >= 0, "Abort count is invalid");
        Validate.notNull(execTimeStats, "Execution time stats is null");

        this.history = Collections.unmodifiableList(new ArrayList<>(history));
        this.topics = extractTopicsFromHistory();
        this.commitCount = commitCount;
        this.abortCount = abortCount;
        this.execTimeStats = execTimeStats.copy();
    }

    public List<TransactionMetadata> getHistory() {
        return history;
    }
    public String getTransactionalId() {
        return getCurrent().transactionalId();
    }
    public TransactionMetadata getCurrent() {
        return history.get(history.size() -1);
    }
    public Set<String> getTopics() {
        return topics;
    }
    public int getCommitCount() {
        return commitCount;
    }
    public int getAbortCount() {
        return abortCount;
    }
    public SummaryStatistics getExecTimeStats() {
        return execTimeStats.copy();
    }

    private Set<String> extractTopicsFromHistory() {
        Set<String> topics = new HashSet<>();
        for (TransactionMetadata metadata : history) {
            if (metadata.topicPartitions().iterator().size() > 0) {
                topics.addAll(
                        ScalaConversions.asJavaSet(metadata.topicPartitions()).stream().
                            map(TopicPartition::topic).
                            collect(Collectors.toSet()));
            }
        }
        return Collections.unmodifiableSet(topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                history,
                commitCount,
                abortCount,
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
        TransactionProjection that = (TransactionProjection)obj;
        return
                Objects.equals(this.history, that.history) &&
                Objects.equals(this.commitCount, that.commitCount) &&
                Objects.equals(this.abortCount, that.abortCount) &&
                Objects.equals(this.execTimeStats, that.execTimeStats);
    }

    @Override
    public String toString() {
        return
                "{history: " + history +
                ", commitCount: " + commitCount +
                ", abortCount: " + abortCount +
                ", execTimeStats: " + execTimeStats +
                "}";
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Queue<TransactionMetadata> history = new CircularFifoQueue<>(HISTORY_SIZE);
        private int commitCount = 0;
        private int abortCount = 0;
        private SummaryStatistics execTimeStats = new SummaryStatistics();

        public Builder() {
            this(null);
        }

        public Builder(TransactionProjection origin) {
            if (origin == null) {
                return;
            }

            this.history.addAll(origin.history);
            this.commitCount = origin.commitCount;
            this.abortCount = origin.abortCount;
            SummaryStatistics.copy(origin.execTimeStats, this.execTimeStats);
        }

        public void append(TransactionMetadata metadata) {
            Validate.notNull(metadata, "Metadata is null");

            history.add(metadata);

            updateCommitCountIfNeeded(metadata);
            updateAbortCountIfNeeded(metadata);
            updateExecTimeStatsIfNeeded(metadata);
        }

        private void updateCommitCountIfNeeded(TransactionMetadata metadata) {
            if (TransactionState.fromScala(metadata.state()) == TransactionState.COMPLETE_COMMIT) {
                commitCount++;
            }
        }

        private void updateAbortCountIfNeeded(TransactionMetadata metadata) {
            if (TransactionState.fromScala(metadata.state()) == TransactionState.COMPLETE_ABORT) {
                abortCount++;
            }
        }

        private void updateExecTimeStatsIfNeeded(TransactionMetadata metadata) {
            if (TransactionState.fromScala(metadata.state()) == TransactionState.COMPLETE_COMMIT) {
                execTimeStats.addValue(metadata.txnLastUpdateTimestamp() - metadata.txnStartTimestamp());
            }
        }

        public TransactionProjection build() {
            return new TransactionProjection(
                    history,
                    commitCount,
                    abortCount,
                    execTimeStats);
        }

    }

}
