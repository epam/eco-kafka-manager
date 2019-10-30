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
package com.epam.eco.kafkamanager.ui.metrics.expose.prometheus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.udmetrics.library.ConsumerGroupPartitionLagMetric;
import com.epam.eco.kafkamanager.udmetrics.library.TopicPartitionOffsetIncreaseMetric;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.dropwizard.DropwizardExports;

/**
 * @author Andrei_Tytsik
 */
public class UdmEnrichingDropwizardExports extends DropwizardExports {

    private static final String NAME_START = " (metric=";
    private static final String NAME_END = ",";

    private final MetricRegistry registry;

    public UdmEnrichingDropwizardExports(MetricRegistry registry) {
        super(registry);

        this.registry = registry;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        // TODO mfSamples = super.collect();
        List<MetricFamilySamples> mfSamples = _collect();
        for (int i = 0; i < mfSamples.size(); i++) {
            MetricFamilySamples samples = mfSamples.get(i);
            String originName = extractOriginMetricName(samples.help);
            if (originName != null) {
                mfSamples.set(i, rebuildUdmMetricIfPossible(samples, originName));
            }
        }
        return mfSamples;
    }

    /*
     * TODO remove when https://github.com/prometheus/client_java/pull/352 fixed
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    private List<MetricFamilySamples> _collect() {
        ArrayList<MetricFamilySamples> mfSamples = new ArrayList<>();
        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            mfSamples.addAll(_from(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            mfSamples.addAll(_from(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            mfSamples.addAll(_from(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            mfSamples.addAll(_from(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            mfSamples.addAll(_from(entry.getKey(), entry.getValue()));
        }
        return mfSamples;
    }

    /*
     * TODO remove when https://github.com/prometheus/client_java/pull/352 fixed
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private List<MetricFamilySamples> _from(String name, Metric metric) {
        Method method = null;
        Boolean accessible = null;
        try {
            String methodName = null;
            Class<?> metricType = null;
            if (metric instanceof Gauge) {
                metricType = Gauge.class;
                methodName = "fromGauge";
            } else if (metric instanceof Counter) {
                metricType = Counter.class;
                methodName = "fromCounter";
            } else if (metric instanceof Histogram) {
                metricType = Histogram.class;
                methodName = "fromHistogram";
            } else if (metric instanceof Timer) {
                metricType = Timer.class;
                methodName = "fromTimer";
            } else if (metric instanceof Meter) {
                metricType = Meter.class;
                methodName = "fromMeter";
            } else {
                throw new RuntimeException(String.format("Unknown metric %s", metric.getClass()));
            }

            method = DropwizardExports.class.getDeclaredMethod(
                    methodName, String.class, metricType);

            accessible = method.isAccessible();
            if (!accessible) {
                method.setAccessible(true);
            }
            return (List<MetricFamilySamples>)method.invoke(this, new Object[]{name, metric});
        } catch (
                NoSuchMethodException |
                SecurityException |
                IllegalAccessException |
                IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof NullPointerException) {
                return new ArrayList<>();
            }
            throw new RuntimeException(ite);
        } finally {
            if (method != null && accessible != null) {
                method.setAccessible(accessible);
            }
        }
    }

    private MetricFamilySamples rebuildUdmMetricIfPossible(MetricFamilySamples samples, String originName) {
        MetricFamilySamples rebuilt = samples;

        String name = null;
        LinkedHashMap<String, String> labels = null;

        Metric metric = registry.getMetrics().get(originName);
        if (metric instanceof ConsumerGroupPartitionLagMetric) {
            labels = extractLabels((ConsumerGroupPartitionLagMetric)metric);
            name = UDMetricType.CONSUMER_GROUP_LAG.name().toLowerCase();
        } else if (metric instanceof TopicPartitionOffsetIncreaseMetric) {
            labels = extractLabels((TopicPartitionOffsetIncreaseMetric)metric);
            name = UDMetricType.TOPIC_OFFSET_INCREASE.name().toLowerCase();
        }

        if (name != null && labels != null) {
            Sample sample = samples.samples.get(0);
            rebuilt = new MetricFamilySamples(
                    name,
                    samples.type,
                    samples.help,
                    Collections.singletonList(
                            new Sample(
                                    name,
                                    new ArrayList<>(labels.keySet()),
                                    new ArrayList<>(labels.values()),
                                    sample.value,
                                    sample.timestampMs)));
        }

        return rebuilt;
    }

    private LinkedHashMap<String, String> extractLabels(ConsumerGroupPartitionLagMetric metric) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        labels.put("group", metric.getGroupName());
        labels.put("topic", metric.getPartition().topic());
        labels.put("partition", "" + metric.getPartition().partition());
        return labels;
    }

    private LinkedHashMap<String, String> extractLabels(TopicPartitionOffsetIncreaseMetric metric) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        labels.put("topic", metric.getTopicPartition().topic());
        labels.put("partition", "" + metric.getTopicPartition().partition());
        return labels;
    }

    public static String extractOriginMetricName(String help) {
        int startIdx = help.indexOf(NAME_START);
        int endIdx = startIdx > 0 ? help.indexOf(NAME_END, startIdx) : -1;
        if (startIdx > 0 && endIdx > 0) {
            return help.substring(startIdx + NAME_START.length(), endIdx);
        } else {
            return null;
        }
    }

}
