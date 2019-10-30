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
package com.epam.eco.kafkamanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class TopicInfoTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        Map<String, ConfigValue> config = new HashMap<>();
        config.put("some.property", new ConfigValue("some.property", "some.value", true, true, false));
        config.put("some.another-property", new ConfigValue("some.another-property", "some.value", true, true, false));
        Map<TopicPartition, PartitionInfo> partitions = new HashMap<>();
        partitions.put(new TopicPartition("topicName", 1), new PartitionInfo(
                "topicName",
                1,
                Arrays.asList(1, 2, 3),
                1,
                Arrays.asList(1, 2, 3)));
        partitions.put(new TopicPartition("topicName", 2), new PartitionInfo(
                "topicName",
                2,
                Arrays.asList(1, 2, 3),
                1,
                Arrays.asList(1, 2, 3)));
        TopicInfo origin = TopicInfo.builder()
                .name("topicName")
                .config(config)
                .partitions(partitions)
                .metadata(Metadata.builder()
                        .description("description")
                        .attribute("a", "a")
                        .updatedBy("me")
                        .updatedAtNow()
                        .build())
                .build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        TopicInfo deserialized = mapper.readValue(
                json,
                TopicInfo.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

}
