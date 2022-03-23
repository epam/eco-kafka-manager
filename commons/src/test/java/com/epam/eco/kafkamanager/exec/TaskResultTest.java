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
package com.epam.eco.kafkamanager.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.PartitionRecordFetchResult;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class TaskResultTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        TaskResult<List<String>> origin = TaskResult.of(() -> Arrays.asList("a", "b", "c"));

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        TaskResult<List<String>> deserialized = mapper.readValue(
                json,
                new TypeReference<TaskResult<List<String>>>(){});
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testSerializedToJsonAndBackRecordFetchResponse() throws Exception {
        Map<TopicPartition, PartitionRecordFetchResult<Object, Object>> recordsInPartitions = new HashMap<>();
        for (int i = 0; i < 1; ++i) {
            TopicPartition topicPartition = new TopicPartition("topic", i);
            List<ConsumerRecord<Object, Object>> list = new ArrayList<>();
            for (int j = 0; j < 1; ++j) {

                RecordHeaders headers = new RecordHeaders();
                headers.add(new RecordHeader("a", "a".getBytes()));
                headers.add(new RecordHeader("b", "b".getBytes()));
                headers.add(new RecordHeader("c", "c".getBytes()));
                list.add(new ConsumerRecord<>(
                        topicPartition.topic(),
                        topicPartition.partition(),
                        j,
                        new Date().getTime(),
                        TimestampType.NO_TIMESTAMP_TYPE,
                        -1,
                        -1,
                        i + " " + j,
                        i + " " + j,
                        headers,
                        Optional.empty()));
            }
            recordsInPartitions.put(
                    topicPartition,
                    PartitionRecordFetchResult.builder()
                            .addRecords(list)
                            .partition(topicPartition)
                            .partitionOffsets(OffsetRange.with(0, 100, true))
                            .scannedOffsets(OffsetRange.with(0, 10, true))
                            .build());
        }
        RecordFetchResult<Object, Object> result = new RecordFetchResult<>(recordsInPartitions);

        TaskResult<RecordFetchResult<Object, Object>> origin = TaskResult.of(() -> result);

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(origin);
        Assert.assertNotNull(json);

        TaskResult<RecordFetchResult<Object, Object>> deserialized = mapper.readValue(
                json,
                new TypeReference<TaskResult<RecordFetchResult<Object, Object>>>(){});

        Assert.assertNotNull(deserialized);
        assertEqualsResults(origin, deserialized);
    }

    private void assertEqualsResults(TaskResult<RecordFetchResult<Object, Object>> a, TaskResult<RecordFetchResult<Object, Object>> b) {
        Assert.assertEquals(a.getError(), b.getError());
        Assert.assertEquals(a.getStartedAt(), b.getStartedAt());
        Assert.assertEquals(a.getFinishedAt(), b.getFinishedAt());
        List<ConsumerRecord<Object, Object>> recordsA = a.getValue().getRecords();
        List<ConsumerRecord<Object, Object>> recordsB = b.getValue().getRecords();
        Assert.assertEquals(recordsA.size(), recordsB.size());
        for (int i = 0; i < recordsA.size(); ++i) {
            ConsumerRecord<Object, Object> recordA = recordsA.get(i);
            ConsumerRecord<Object, Object> recordB = recordsB.get(i);

            Assert.assertEquals(recordA.topic(), recordB.topic());
            Assert.assertEquals(recordA.partition(), recordB.partition());
            Assert.assertEquals(recordA.timestampType(), recordB.timestampType());
            Assert.assertEquals(recordA.timestamp(), recordB.timestamp());
            Assert.assertEquals(recordA.serializedKeySize(), recordB.serializedKeySize());
            Assert.assertEquals(recordA.serializedValueSize(), recordB.serializedValueSize());
            Assert.assertEquals(recordA.offset(), recordB.offset());
            Assert.assertEquals(recordA.headers(), recordB.headers());
            Assert.assertEquals(recordA.key(), recordB.key());
            Assert.assertEquals(recordA.value(), recordB.value());
        }
    }

}
