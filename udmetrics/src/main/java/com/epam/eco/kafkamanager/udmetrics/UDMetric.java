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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Andrei_Tytsik
 */
public final class UDMetric implements Comparable<UDMetric> {

    private final UDMetricConfig config;
    private final List<Metric> metrics;
    private final List<String> errors;

    private final int hashCode;
    private final String toString;

    private UDMetric(
            UDMetricConfig config,
            Collection<Metric> metrics,
            Collection<String> errors) {
        Validate.notNull(config, "Config is null");
        if (!CollectionUtils.isEmpty(metrics)) {
            Validate.noNullElements(metrics, "Collection of metrics contain null elements");
        }
        if (!CollectionUtils.isEmpty(errors)) {
            Validate.noNullElements(errors, "Collection of errors contain null elements");
        }

        this.config = config;
        this.metrics =
                !CollectionUtils.isEmpty(metrics) ?
                Collections.unmodifiableList(new ArrayList<>(metrics)) :
                Collections.emptyList();
        this.errors =
                !CollectionUtils.isEmpty(errors) ?
                Collections.unmodifiableList(new ArrayList<>(errors)) :
                Collections.emptyList();

        hashCode = calculateHashCode();
        toString = calculateToString();
    }

    public String getName() {
        return config.getName();
    }
    public UDMetricType getType() {
        return config.getType();
    }
    public String getResourceName() {
        return config.getResourceName();
    }
    public Map<String, Object> getConfig() {
        return config.getConfig();
    }
    public List<Metric> getMetrics() {
        return metrics;
    }
    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        UDMetric that = (UDMetric)obj;
        return
                Objects.equals(this.config, that.config) &&
                Objects.equals(this.metrics, that.metrics) &&
                Objects.equals(this.errors, that.errors);
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int compareTo(UDMetric that) {
        return ObjectUtils.compare(this.config, that.config);
    }

    private int calculateHashCode() {
        return Objects.hash(config, metrics, errors);
    }

    private String calculateToString() {
        return
                "{config: " + config +
                ", metrics: " + metrics +
                ", errors: " + errors +
                "}";
    }

    public static UDMetric with(UDMetricConfig config, Collection<Metric> metrics) {
        return new UDMetric(config, metrics, null);
    }

    public static UDMetric withErrors(UDMetricConfig config, Collection<String> errors) {
        return new UDMetric(config, null, errors);
    }

}
