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
package com.epam.eco.kafkamanager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import com.epam.eco.commons.kafka.serde.jackson.KafkaModule;
import com.epam.eco.kafkamanager.rest.helper.SpringModule;

/**
 * @author Andrei_Tytsik
 */
public final class TestObjectMapperSingleton {

    private static final ObjectMapper objectMapper = new ObjectMapper().
            registerModule(new ParameterNamesModule()).
            registerModule(new Jdk8Module()).
            registerModule(new JavaTimeModule()).
            registerModule(new SpringModule()).
            registerModule(new KafkaModule());

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private TestObjectMapperSingleton() {
    }

}
