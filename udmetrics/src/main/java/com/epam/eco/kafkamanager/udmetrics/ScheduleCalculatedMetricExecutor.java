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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.udmetrics.autoconfigure.UDMetricManagerProperties;

/**
 * @author Andrei_Tytsik
 */
public class ScheduleCalculatedMetricExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCalculatedMetricExecutor.class);

    @Autowired
    private UDMetricManager udMetricManager;

    @Autowired
    private UDMetricManagerProperties metricManagerProperties;

    private ScheduledExecutorService executorService;

    @PostConstruct
    private void init() {
        initExecutorService();
    }

    @PreDestroy
    private void destroy() {
        shutdownExecutorService();
    }

    private void initExecutorService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(
                () ->  calculate(),
                metricManagerProperties.getCalculationIntervalInMs() / 2,
                metricManagerProperties.getCalculationIntervalInMs(),
                TimeUnit.MILLISECONDS);
    }

    private void shutdownExecutorService() {
        executorService.shutdownNow();
    }

    public void calculate() {
        udMetricManager.listAll().forEach(udm -> {
            udm.getMetrics().forEach(metric -> {
                try {
                    if (metric instanceof ScheduleCalculatedMetric) {
                        ((ScheduleCalculatedMetric)metric).calculateValue();
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Failed to calculate value for schedule-calculated metric " + metric, ex);
                }
            });
        });
    }

}
