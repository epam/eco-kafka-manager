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
package com.epam.eco.kafkamanager.rest.request;

import java.util.HashMap;

import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class ConsumerGroupOffsetResetRequestTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        HashMap<TopicPartition, Long> offsets = new HashMap<>();
        offsets.put(new TopicPartition("topic", 1), 1L);
        offsets.put(new TopicPartition("topic", 2), 2L);
        offsets.put(new TopicPartition("topic", 3), 3L);
        offsets.put(new TopicPartition("topic", 4), 4L);
        offsets.put(new TopicPartition("topic", 5), 5L);
        ConsumerGroupOffsetResetRequest origin = new ConsumerGroupOffsetResetRequest("groupName", offsets);

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(origin);
        Assert.assertNotNull(json);

        ConsumerGroupOffsetResetRequest deserialized = mapper.readValue(
                json,
                ConsumerGroupOffsetResetRequest.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(deserialized, origin);
    }
}
