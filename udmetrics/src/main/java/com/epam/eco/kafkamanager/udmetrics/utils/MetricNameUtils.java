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
package com.epam.eco.kafkamanager.udmetrics.utils;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;

/**
 * @author Andrei_Tytsik
 */
public class MetricNameUtils {

    private static final String METRIC_NAME_UNALOWED_CHAR_REGEX = "[^\\w\\-\\/]{1}";

    public static String concatenateNames(String name, String... names) {
        return MetricRegistry.name(name, names);
    }

    public static String sanitizeAndConcatenateNames(String name, String... names) {
        return concatenateNames(
                sanitizeName(name),
                sanitizeNames(names));
    }

    public static String sanitizeName(String name) {
        if (name == null) {
            return null;
        }

        return name.replaceAll(METRIC_NAME_UNALOWED_CHAR_REGEX, "_");
    }

    public static String[] sanitizeNames(String[] names) {
        if (names == null) {
            return null;
        }

        String[] sanitizedNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            sanitizedNames[i] = sanitizeName(names[i]);
        }
        return sanitizedNames;
    }

    public static Set<String> extractConcatenatedNames(String name, Metric metric) {
        Set<String> concatenatedNames = new HashSet<>();
        collectConcatenatedNames(name, metric, concatenatedNames);
        return concatenatedNames;
    }

    private static void collectConcatenatedNames(
            String name,
            Metric metric,
            Set<String> concatenatedNames) {
        if (metric instanceof MetricSet) {
            for (Entry<String, Metric> entry : ((MetricSet)metric).getMetrics().entrySet()) {
                String nameChild = entry.getKey();
                Metric metricChild = entry.getValue();
                collectConcatenatedNames(
                        MetricRegistry.name(name, nameChild),
                        metricChild,
                        concatenatedNames);
            }
        } else {
            concatenatedNames.add(name);
        }
    }

}
