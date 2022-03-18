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

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.kafkamanager.TransactionMetadataInfo;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class TransactionMetadataInfoWrapper {

    private final TransactionMetadataInfo metadataInfo;

    public TransactionMetadataInfoWrapper(TransactionMetadataInfo metadataInfo) {
        Validate.notNull(metadataInfo, "Transaction Metadata info is null");

        this.metadataInfo = metadataInfo;
    }

    public static TransactionMetadataInfoWrapper wrap(TransactionMetadataInfo metadataInfo) {
        return new TransactionMetadataInfoWrapper(metadataInfo);
    }

    public String getTransactionalId() {
        return metadataInfo.getTransactionalId();
    }
    public long getProducerId() {
        return metadataInfo.getProducerId();
    }
    public short getProducerEpoch() {
        return metadataInfo.getProducerEpoch();
    }
    public int getTimeoutMs() {
        return metadataInfo.getTimeoutMs();
    }
    public TransactionState getState() {
        return metadataInfo.getState();
    }
    public List<TopicPartition> getPartitions() {
        return metadataInfo.getPartitions();
    }
    public LocalDateTime getStartDate() {
        return metadataInfo.getStartDate();
    }
    public LocalDateTime getLastUpdateDate() {
        return metadataInfo.getLastUpdateDate();
    }

    public CollapsedCollectionIterable<String> getPartitionsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                getPartitions(),
                TopicPartition::toString,
                size);
    }

}
