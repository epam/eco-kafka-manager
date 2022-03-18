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
package com.epam.eco.kafkamanager.ui.brokers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.EndPointInfo;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.ui.common.ConfigEntryWrapper;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class BrokerInfoWrapper {

    private final BrokerInfo brokerInfo;
    private final KafkaAdminOperations adminOperations;

    private Map<String, ConfigEntryWrapper> allConfigEntries;

    public BrokerInfoWrapper(BrokerInfo brokerInfo, KafkaAdminOperations adminOperations) {
        Validate.notNull(brokerInfo, "Broker info is null");
        Validate.notNull(adminOperations, "Kafka admin operations is null");

        this.brokerInfo = brokerInfo;
        this.adminOperations = adminOperations;
    }

    public static BrokerInfoWrapper wrap(BrokerInfo brokerInfo, KafkaAdminOperations adminOperations) {
        return new BrokerInfoWrapper(brokerInfo, adminOperations);
    }

    public int getId() {
        return brokerInfo.getId();
    }
    public List<EndPointInfo> getEndPoints() {
        return brokerInfo.getEndPoints();
    }
    public String getRack() {
        return brokerInfo.getRack();
    }
    public int getVersion() {
        return brokerInfo.getVersion();
    }
    public int getJmxPort() {
        return brokerInfo.getJmxPort();
    }
    public String getJmxPortDisplayString() {
        return brokerInfo.getJmxPort() > 0 ? "" + brokerInfo.getJmxPort() : "";
    }
    public Map<String, String> getConfig() {
        return brokerInfo.getConfig();
    }
    public Map<String, ConfigEntryWrapper> getAllConfigEntries() {
        if (allConfigEntries == null) {
            Config config = adminOperations.describeBrokerConfig(getId());
            allConfigEntries = new TreeMap<>();
            config.entries().forEach(
                    e -> allConfigEntries.put(e.name(), ConfigEntryWrapper.wrapForBroker(e)));
        }
        return allConfigEntries;
    }
    public String getMetadataDescription() {
        return brokerInfo.getMetadata().map(Metadata::getDescription).orElse(null);
    }
    public Metadata getMetadata() {
        return brokerInfo.getMetadata().orElse(null);
    }

    public CollapsedCollectionIterable<String> getEndPointsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                brokerInfo.getEndPoints(),
                EndPointInfo::toString,
                size);
    }

}
