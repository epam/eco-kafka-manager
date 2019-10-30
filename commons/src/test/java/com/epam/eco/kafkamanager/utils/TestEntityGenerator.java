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
package com.epam.eco.kafkamanager.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.security.auth.SecurityProtocol;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.ConfigValue;
import com.epam.eco.kafkamanager.EndPointInfo;
import com.epam.eco.kafkamanager.Metadata;

/**
 * @author Raman_Babich
 */
public final class TestEntityGenerator {

    public static EndPointInfo endPointInfo(int port) {
        return EndPointInfo.with(SecurityProtocol.PLAINTEXT, "host", port);
    }

    public static List<EndPointInfo> endPointInfoList(int amount) {
        Validate.isTrue(amount >= 0, "Amount should be greater or equals to 0");

        return IntStream.range(0, amount)
                .mapToObj(TestEntityGenerator::endPointInfo)
                .collect(Collectors.toList());
    }

    public static ConfigValue configValue(String name) {
        return new ConfigValue(name, name + "-value");
    }

    public static Map<String, ConfigValue> brokerConfigMap(int propertiesAmount) {
        Validate.isTrue(propertiesAmount >= 0, "Properties amount should be greater or equals to 0");

        Map<String, ConfigValue> configs = new HashMap<>();
        for (int i = 0; i < propertiesAmount; ++i) {
            String name = Integer.toString(i);
            configs.put(name, configValue(name));
        }
        return configs;
    }

    public static Metadata metadata() {
        return Metadata.builder().
                description("description").
                appendAttribute("key1", "value1").
                appendAttribute("key2", "value2").
                appendAttribute("key3", "value3").
                updatedAtNow().
                updatedBy("test").
                build();
    }

    public static BrokerInfo brokerInfo(int id) {
        return BrokerInfo.builder()
                .id(id)
                .endPoints(endPointInfoList(5))
                .config(brokerConfigMap(10))
                .rack("rack-" + id)
                .metadata(metadata())
                .build();
    }

    public static List<BrokerInfo> brokerInfoList(int amount) {
        Validate.isTrue(amount >= 0, "Amount should be greater or equals to 0");

        return IntStream.range(0, amount)
                .mapToObj(TestEntityGenerator::brokerInfo)
                .collect(Collectors.toList());
    }

    private TestEntityGenerator() {
    }
}
