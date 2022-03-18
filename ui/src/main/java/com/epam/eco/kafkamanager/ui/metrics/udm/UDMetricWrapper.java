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
package com.epam.eco.kafkamanager.ui.metrics.udm;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.udmetrics.Metric;
import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable;

/**
 * @author Andrei_Tytsik
 */
public class UDMetricWrapper {

    private final UDMetric udm;

    public UDMetricWrapper(UDMetric udm) {
        Validate.notNull(udm, "UDM is null");

        this.udm = udm;
    }

    public String getName() {
        return udm.getName();
    }

    public UDMetricType getType() {
        return udm.getType();
    }

    public String getResourceName() {
        return udm.getResourceName();
    }

    public Map<String, Object> getConfig() {
        return udm.getConfig();
    }

    public Collection<Metric> getMetrics() {
        return udm.getMetrics();
    }

    public boolean isTopicResource() {
        return udm.getType() == UDMetricType.TOPIC_OFFSET_INCREASE;
    }

    public boolean isConsumerGroupResource() {
        return udm.getType() == UDMetricType.CONSUMER_GROUP_LAG;
    }

    public boolean isOtherResource() {
        return !isTopicResource() && !isConsumerGroupResource();
    }

    public boolean hasErrors() {
        return udm.hasErrors();
    }

    public String getConfigAsString() {
        Map<String, Object> config = udm.getConfig();
        if (config == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Entry<String, Object> configEntry : config.entrySet()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(configEntry.getKey()).append(": ").append(configEntry.getValue());
        }
        return builder.toString();
    }

    public CollapsedCollectionIterable<String> getMetricsAsCollapsedCol(int size) {
        return new CollapsedCollectionIterable<>(
                udm.getMetrics(),
                Objects::toString,
                size);
    }

    public static UDMetricWrapper wrap(UDMetric udm) {
        return new UDMetricWrapper(udm);
    }

    public String getUrl() {
        if (UDMetricType.CONSUMER_GROUP_LAG == udm.getType()) {
            return ConsumerGroupLagUDMController.buildMetricUrl(udm);
        } else if (UDMetricType.TOPIC_OFFSET_INCREASE == udm.getType()) {
                return TopicOffsetIncreaseUDMController.buildMetricUrl(udm);
        } else {
            throw new RuntimeException(
                    String.format(
                            "No supported controller found for UDM = %s",
                            udm.getConfig().getClass().getName()));
        }
    }

}
