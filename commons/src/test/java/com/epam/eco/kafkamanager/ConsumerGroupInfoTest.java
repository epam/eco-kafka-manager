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
package com.epam.eco.kafkamanager;

import java.time.LocalDateTime;
import java.util.Collections;

import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class ConsumerGroupInfoTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        ConsumerGroupInfo origin = ConsumerGroupInfo.builder()
                .name("consumerGroupName")
                .coordinator(1)
                .state(ConsumerGroupState.STABLE)
                .protocolType("consumer")
                .partitionAssignor("range")
                .addMember(ConsumerGroupMemberInfo.builder()
                        .clientId("1")
                        .heartbeatSatisfied(true)
                        .clientHost("1.1.1.1")
                        .memberId("memberId-1")
                        .rebalanceTimeoutMs(1000)
                        .sessionTimeoutMs(10000)
                        .assignment(Collections.singleton(new TopicPartition("topic", 0)))
                        .build())
                .addMember(ConsumerGroupMemberInfo.builder()
                        .clientId("2")
                        .heartbeatSatisfied(false)
                        .clientHost("2.2.2.2")
                        .memberId("memberId-2")
                        .rebalanceTimeoutMs(1000)
                        .sessionTimeoutMs(10000)
                        .assignment(Collections.singleton(new TopicPartition("topic", 0)))
                        .build())
                .storageType(ConsumerGroupInfo.StorageType.KAFKA)
                .addOffsetsAndMetadata(OffsetAndMetadataInfo.builder()
                        .offset(0)
                        .commitDate(LocalDateTime.now())
                        .expireDate(LocalDateTime.now())
                        .topicPartition(new TopicPartition("topicName", 0))
                        .metadata("metadata")
                        .build())
                .addOffsetsAndMetadata(OffsetAndMetadataInfo.builder()
                        .offset(0)
                        .commitDate(LocalDateTime.now())
                        .expireDate(LocalDateTime.now())
                        .topicPartition(new TopicPartition("topicName", 1))
                        .metadata("metadata")
                        .build())
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

        ConsumerGroupInfo deserialized = mapper.readValue(
                json,
                ConsumerGroupInfo.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

}
