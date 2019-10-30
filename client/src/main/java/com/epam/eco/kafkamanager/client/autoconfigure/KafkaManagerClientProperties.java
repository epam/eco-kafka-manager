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
package com.epam.eco.kafkamanager.client.autoconfigure;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Raman_Babich
 */
@ConfigurationProperties(prefix = "eco.kafkamanager.client")
public class KafkaManagerClientProperties {

    private String kafkaManagerUrl;

    @PostConstruct
    public void init() {
        Validate.notNull(kafkaManagerUrl, "Property 'eco.kafkamanager.client.kafkaManagerUrl' is not specified");
    }

    public String getKafkaManagerUrl() {
        return kafkaManagerUrl;
    }

    public void setKafkaManagerUrl(String kafkaManagerUrl) {
        this.kafkaManagerUrl = kafkaManagerUrl;
    }
}
