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
package com.epam.eco.kafkamanager.ui.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.kafkamanager.Statistics;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class TransactionInfoWrapper {

    private final TransactionInfo transactionInfo;

    private TransactionMetadataInfoWrapper current;
    private List<TransactionMetadataInfoWrapper> history;

    public TransactionInfoWrapper(TransactionInfo transactionInfo) {
        Validate.notNull(transactionInfo, "Transaction info is null");

        this.transactionInfo = transactionInfo;
    }

    public static TransactionInfoWrapper wrap(TransactionInfo transactionInfo) {
        return new TransactionInfoWrapper(transactionInfo);
    }

    public String getTransactionalId() {
        return transactionInfo.getTransactionalId();
    }
    public List<TransactionMetadataInfoWrapper> getHistory() {
        if (history == null) {
            history = transactionInfo.getHistory().stream().
                    map(TransactionMetadataInfoWrapper::wrap).
                    collect(Collectors.toList());
            Collections.reverse(history);
        }
        return history;
    }
    public Set<String> getTopicNames() {
        return transactionInfo.getTopicNames();
    }
    public Integer getSuccessRpm() {
        return transactionInfo.getSuccessTimeSeries().currentRatePerMinute();
    }
    public Integer getFailRpm() {
        return transactionInfo.getFailTimeSeries().currentRatePerMinute();
    }
    public Statistics getExecTimeStats() {
        return transactionInfo.getExecTimeStats();
    }
    public double getExecMinTime() {
        return roundExecTime(getExecTimeStats().getMin());
    }
    public double getExecMaxTime() {
        return roundExecTime(getExecTimeStats().getMax());
    }
    public double getExecMeanTime() {
        return roundExecTime(getExecTimeStats().getMean());
    }
    public long getProducerId() {
        return current().getProducerId();
    }
    public short getProducerEpoch() {
        return current().getProducerEpoch();
    }
    public int getTimeoutMs() {
        return current().getTimeoutMs();
    }
    public TransactionState getState() {
        return current().getState();
    }
    public List<TopicPartition> getPartitions() {
        return current().getPartitions();
    }
    public LocalDateTime getStartDate() {
        return current().getStartDate();
    }
    public LocalDateTime getLastUpdateDate() {
        return current().getLastUpdateDate();
    }

    public CollapsedCollectionIterable<String> getPartitionsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                getPartitions(),
                TopicPartition::toString,
                size);
    }

    private TransactionMetadataInfoWrapper current() {
        if (current == null) {
            current = TransactionMetadataInfoWrapper.wrap(transactionInfo.getCurrentMetadata());
        }
        return current;
    }

    private static double roundExecTime(double time) {
        if (!Double.isFinite(time)) {
            return time;
        }

        BigDecimal value = new BigDecimal(time);
        value = value.setScale(2, RoundingMode.HALF_UP);
        return value.doubleValue();
    }

}
