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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.codahale.metrics.MetricRegistry;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;


/**
 * @author Andrei_Tytsik
 */
@ConditionalOnProperty(name = "endpoints.prometheus.enabled", havingValue = "true")
@EnablePrometheusEndpoint
public class PrometheusMetricsExposeConfiguration {

    @Autowired
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void postConstruct() {
        new UdmEnrichingDropwizardExports(metricRegistry).register();
    }

}
