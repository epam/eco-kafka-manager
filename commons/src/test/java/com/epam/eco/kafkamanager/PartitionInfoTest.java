/*
 * Copyright 2020 EPAM Systems
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

import java.util.Collections;

import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class PartitionInfoTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        PartitionInfo origin = PartitionInfo.builder().
                id(new TopicPartition("topic", 0)).
                replicas(Collections.singletonList(0)).
                leader(0).
                isr(Collections.singletonList(0)).
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        PartitionInfo deserialized = mapper.readValue(
                json,
                PartitionInfo.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

}
