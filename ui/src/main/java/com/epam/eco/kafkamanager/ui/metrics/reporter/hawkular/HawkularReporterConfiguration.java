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
package com.epam.eco.kafkamanager.ui.metrics.reporter.hawkular;

import java.util.concurrent.TimeUnit;

import org.hawkular.metrics.dropwizard.HawkularReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.MetricRegistry;


/**
 * @author Andrei_Tytsik
 */
@ConditionalOnProperty(
        name = "eco.kafkamanager.ui.metrics.expose.hawkular.enabled", havingValue = "true")
@EnableConfigurationProperties(HawkularReporterProperties.class)
public class HawkularReporterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HawkularReporterConfiguration.class);

    @Autowired
    private HawkularReporterProperties properties;

    @Autowired
    private MetricRegistry metricRegistry;

    @Bean
    public HawkularReporter hawkularReporter() {
        LOGGER.info("Creating HawkularReporter bean");

        HawkularReporter reporter = HawkularReporter.
                builder(metricRegistry, properties.getTenant()).
                uri(properties.getUrl()).
                basicAuth(properties.getUsername(), properties.getPassword()).
                build();
        reporter.start(properties.getPollInvervalInMs(), TimeUnit.MILLISECONDS);

        LOGGER.info("HawkularReporter bean created");

        return reporter;
    }

}
