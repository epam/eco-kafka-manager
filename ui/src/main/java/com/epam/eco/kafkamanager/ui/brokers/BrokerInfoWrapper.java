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
package com.epam.eco.kafkamanager.ui.brokers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.ConfigValue;
import com.epam.eco.kafkamanager.EndPointInfo;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class BrokerInfoWrapper {

    private final BrokerInfo brokerInfo;

    public BrokerInfoWrapper(BrokerInfo brokerInfo) {
        Validate.notNull(brokerInfo, "Broker info is null");

        this.brokerInfo = brokerInfo;
    }

    public static BrokerInfoWrapper wrap(BrokerInfo brokerInfo) {
        return new BrokerInfoWrapper(brokerInfo);
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
    public Map<String, ConfigValue> getConfig() {
        return brokerInfo.getConfig();
    }
    public List<ConfigValue> getConfigValues() {
        return brokerInfo.getConfig().values().stream().collect(Collectors.toList());
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
