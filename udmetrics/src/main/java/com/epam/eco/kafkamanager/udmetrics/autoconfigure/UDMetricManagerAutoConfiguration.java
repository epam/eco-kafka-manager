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
package com.epam.eco.kafkamanager.udmetrics.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.epam.eco.kafkamanager.udmetrics.ScheduleCalculatedMetricExecutor;
import com.epam.eco.kafkamanager.udmetrics.UDMetricManager;
import com.epam.eco.kafkamanager.udmetrics.UDMetricManagerImpl;
import com.epam.eco.kafkamanager.udmetrics.config.repo.kafka.KafkaUDMetricConfigRepoConfiguration;
import com.epam.eco.kafkamanager.udmetrics.library.ConsumerGroupLagUDMCreator;
import com.epam.eco.kafkamanager.udmetrics.library.TopicOffsetIncreaseUDMCreator;

/**
 * @author Andrei_Tytsik
 */
@Configuration
@ConditionalOnProperty(name = "eco.kafkamanager.udmetrics.enabled", havingValue = "true")
@EnableConfigurationProperties(UDMetricManagerProperties.class)
@Import(KafkaUDMetricConfigRepoConfiguration.class)
public class UDMetricManagerAutoConfiguration {

    @Bean
    public UDMetricManager udMetricManager() {
        return new UDMetricManagerImpl();
    }

    @Bean
    public ConsumerGroupLagUDMCreator consumerGroupLagUDMCreator() {
        return new ConsumerGroupLagUDMCreator();
    }

    @Bean
    public TopicOffsetIncreaseUDMCreator topicOffsetIncreaseUDMCreator() {
        return new TopicOffsetIncreaseUDMCreator();
    }

    @Bean
    public ScheduleCalculatedMetricExecutor scheduleCalculatedMetricExecutor() {
        return new ScheduleCalculatedMetricExecutor();
    }

}
