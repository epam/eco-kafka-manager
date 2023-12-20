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
package com.epam.eco.kafkamanager.rest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.Module;

import com.epam.eco.commons.kafka.serde.jackson.KafkaModule;
import com.epam.eco.kafkamanager.rest.helper.SpringModule;

/**
 * @author Raman_Babich
 */
@Configuration
@EnableConfigurationProperties(KafkaManagerRestProperties.class)
public class KafkaManagerRestConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(Boolean.FALSE);
    }

    @Bean
    public AuthenticationLogFilter authenticationLogFilter() {
        return new AuthenticationLogFilter();
    }

    @Bean
    public Module kafkaModule() {
        return new KafkaModule();
    }

    @Bean
    public Module springModule() {
        return new SpringModule();
    }

}
