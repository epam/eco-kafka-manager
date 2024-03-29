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
package com.epam.eco.kafkamanager.core.authz.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.epam.eco.kafkamanager.Authorizer;

/**
 * @author Andrei_Tytsik
 */
@EnableConfigurationProperties(KafkaAuthorizerProperties.class)
@ConditionalOnProperty(
        name="eco.kafkamanager.core.authz.kafka.enabled",
        havingValue="true")
public class KafkaAuthorizerConfiguration {

    @Bean
    public Authorizer authorizer() {
        return new KafkaAuthorizer();
    }

}
