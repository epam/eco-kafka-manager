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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

/**
 * @author Andrei_Tytsik
 */
public final class UDMetric implements MetricSet, Comparable<UDMetric> {

    private final UDMetricConfig config;
    private final Map<String, Metric> metrics;
    private final List<String> errors;

    private final int hashCode;
    private final String toString;

    public UDMetric(
            UDMetricConfig config,
            List<String> errors) {
        Validate.notNull(config, "Config is null");
        Validate.notEmpty(errors, "Collection of errors is null or empty");

        this.config = config;
        this.metrics = Collections.emptyMap();
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));

        hashCode = calculateHashCode();
        toString = calculateToString();
    }

    public UDMetric(
            UDMetricConfig config,
            Map<String, Metric> metrics) {
        Validate.notNull(config, "Config is null");
        Validate.notEmpty(metrics, "Metrics map is null or empty");
        Validate.noNullElements(
                metrics.keySet(), "Collection of metric keys contain null elements");
        Validate.noNullElements(
                metrics.values(), "Collection of metric values contain null elements");

        this.config = config;
        this.metrics = Collections.unmodifiableMap(new LinkedHashMap<>(metrics));
        this.errors = Collections.emptyList();

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
    @Override
    public Map<String, Metric> getMetrics() {
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

    public static UDMetric with(
            UDMetricConfig config,
            Map<String, Metric> metrics) {
        return new UDMetric(config, metrics);
    }

    public static UDMetric with(
            UDMetricConfig config,
            List<String> errors) {
        return new UDMetric(config, errors);
    }

}
