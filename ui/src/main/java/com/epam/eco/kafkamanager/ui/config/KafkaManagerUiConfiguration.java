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
package com.epam.eco.kafkamanager.ui.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.epam.eco.kafkamanager.ui.GlobalModelEnrichingInterceptor;
import com.epam.eco.kafkamanager.ui.LogoutListener;
import com.epam.eco.kafkamanager.ui.topics.DataCatalogUrlResolver;
import com.epam.eco.kafkamanager.ui.topics.GrafanaMetricsUrlResolver;
import com.epam.eco.kafkamanager.ui.topics.SchemaCatalogUrlResolver;

/**
 * @author Andrei_Tytsik
 */
@Configuration
@EnableConfigurationProperties(KafkaManagerUiProperties.class)
public class KafkaManagerUiConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(Boolean.FALSE);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(modelHandlerInterceptor());
    }

    @Bean
    public GlobalModelEnrichingInterceptor modelHandlerInterceptor() {
        return new GlobalModelEnrichingInterceptor();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public LogoutListener logoutListener() {
        return new LogoutListener();
    }

    @Bean
    public DataCatalogUrlResolver dataCatalogUrlResolver(KafkaManagerUiProperties properties) {
        return new DataCatalogUrlResolver(properties);
    }

    @Bean
    public SchemaCatalogUrlResolver schemaCatalogUrlResolver(KafkaManagerUiProperties properties) {
        return new SchemaCatalogUrlResolver(properties);
    }
    @Bean
    public GrafanaMetricsUrlResolver grafanaMetricsUrlResolver(KafkaManagerUiProperties properties) {
        return new GrafanaMetricsUrlResolver(properties);
    }

    @Bean(initMethod = "init")
    @ConditionalOnProperty(name="eco.kafkamanager.ui.topicBrowser.useCache", havingValue="true")
    public TopicOffsetCacheCleanerRunner topicOffsetCacheCleanerRunner(KafkaManagerUiProperties properties) {
        return new TopicOffsetCacheCleanerRunner(properties.getTopicBrowser().getCacheCleanerIntervalMin());
    }

    @Bean(initMethod = "init")
    public TopicOffsetRangeCacheCleanerRunner topicOffsetRangeCacheCleanerRunner(KafkaManagerUiProperties properties) {
        return new TopicOffsetRangeCacheCleanerRunner(properties.getTopicBrowser().getCacheCleanerIntervalMin());
    }
}
