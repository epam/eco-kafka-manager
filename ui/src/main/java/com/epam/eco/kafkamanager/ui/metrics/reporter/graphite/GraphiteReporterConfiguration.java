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
package com.epam.eco.kafkamanager.ui.metrics.reporter.graphite;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;


/**
 * @author Andrei_Tytsik
 */
@ConditionalOnProperty(
        name = "eco.kafkamanager.ui.metrics.expose.graphite.enabled", havingValue = "true")
@EnableConfigurationProperties(GraphiteReporterProperties.class)
public class GraphiteReporterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporterConfiguration.class);

    @Autowired
    private GraphiteReporterProperties properties;

    @Autowired
    private MetricRegistry metricRegistry;

    @Bean
    public GraphiteReporter graphiteReporter() {
        LOGGER.info("Creating GraphiteReporter bean");

        Graphite graphite = new Graphite(
                new InetSocketAddress(
                        properties.getHost(),
                        properties.getPort()));
        GraphiteReporter reporter = GraphiteReporter.
                forRegistry(metricRegistry).
                //prefixedWith(MetricRegistry.name(properties.getPrefix(), InetAddressUtils.getHostName())).
                prefixedWith(MetricRegistry.name(properties.getPrefix())).
                convertRatesTo(TimeUnit.SECONDS).
                convertDurationsTo(TimeUnit.SECONDS).
                filter(MetricFilter.ALL).
                build(graphite);
        reporter.start(properties.getPollInvervalInMs(), TimeUnit.MILLISECONDS);

        LOGGER.info("GraphiteReporter bean created");

        return reporter;
    }

}
