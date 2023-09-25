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

import com.epam.eco.kafkamanager.PartitionByKeyResolver;
import com.epam.eco.kafkamanager.PartitionByKeyResolverImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.epam.eco.kafkamanager.ui.GlobalModelEnrichingInterceptor;
import com.epam.eco.kafkamanager.ui.LogoutListener;

/**
 * @author Andrei_Tytsik
 */
@Configuration
@Import({SpelExpressionConverterConfiguration.class})
@EnableConfigurationProperties(KafkaManagerUiProperties.class)
public class KafkaManagerUiConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(Boolean.FALSE);
    }
    @Bean
    public static KafkaManagerUiPropertiesValidator configurationPropertiesValidator() {
        return new KafkaManagerUiPropertiesValidator();
    }

    @Bean
    public PropertiesInitializer propertiesInitializer(KafkaManagerUiProperties properties) {
        return new PropertiesInitializer(properties);
    }
    @Bean
    public PartitionByKeyResolver partitionByKeyResolver() {
        return new PartitionByKeyResolverImpl();
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

    @Bean(initMethod = "init")
    @ConditionalOnProperty(name="eco.kafkamanager.ui.topicBrowser.useCache", havingValue="true")
    public TopicOffsetCacheCleanerRunner topicOffsetCacheCleanerRunner(KafkaManagerUiProperties properties) {
        return new TopicOffsetCacheCleanerRunner(properties.getTopicBrowser().getCacheCleanerIntervalMin());
    }

}
