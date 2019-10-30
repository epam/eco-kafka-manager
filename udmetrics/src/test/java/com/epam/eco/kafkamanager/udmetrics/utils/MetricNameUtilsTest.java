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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

/**
 * @author Andrei_Tytsik
 */
public class MetricNameUtilsTest {

    @Test
    public void testNamesAreConcatenated() throws Exception {
        String concatenated = MetricNameUtils.concatenateNames("a", "b", "c");

        Assert.assertNotNull(concatenated);
        Assert.assertEquals("a.b.c", concatenated);
    }

    @Test
    public void testNameIsSanitized() throws Exception {
        String sanitized = MetricNameUtils.sanitizeName("~!@#$%^&*()_+[]{};:<>,?'\"");

        Assert.assertNotNull(sanitized);
        Assert.assertEquals("_________________________", sanitized);
    }

    @Test
    public void testNamesAreExtractedAndConcatenated() throws Exception {
        Metric metricA = new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                Map<String, Metric> metricsB = new HashMap<>();
                metricsB.put("b1", new Metric(){});
                metricsB.put("b2", new Metric(){});
                metricsB.put("b3", new MetricSet() {
                    @Override
                    public Map<String, Metric> getMetrics() {
                        Map<String, Metric> metricsC = new HashMap<>();
                        metricsC.put("c1", new Metric(){});
                        metricsC.put("c2", new Metric(){});
                        metricsC.put("c3", new Metric(){});
                        return metricsC;
                    }
                });
                return metricsB;
            }
        };

        Set<String> extractedNames = MetricNameUtils.extractConcatenatedNames("a", metricA);

        Assert.assertNotNull(extractedNames);
        Assert.assertTrue(extractedNames.size() == 5);
        Assert.assertTrue(extractedNames.contains("a.b1"));
        Assert.assertTrue(extractedNames.contains("a.b2"));
        Assert.assertTrue(extractedNames.contains("a.b3.c1"));
        Assert.assertTrue(extractedNames.contains("a.b3.c2"));
        Assert.assertTrue(extractedNames.contains("a.b3.c3"));
    }

}
