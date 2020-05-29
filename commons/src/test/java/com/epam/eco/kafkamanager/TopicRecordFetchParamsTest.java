package com.epam.eco.kafkamanager;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

public class TopicRecordFetchParamsTest {

    private static final long LIMIT = 10L;
    private static final long TIMEOUT_IN_MS = 10000L;

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        Map<Integer, Long> offsets = new HashMap<>();
        offsets.put(0, 15L);
        offsets.put(1, 25L);

        TopicRecordFetchParams origin1 = TopicRecordFetchParams.byOffsets(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.STRING,
                LIMIT,
                TIMEOUT_IN_MS,
                offsets);

        Map<Integer, Long> partitionTimestamp = new HashMap<>();
        partitionTimestamp.put(0, 1585101630000L);
        partitionTimestamp.put(1, 2585101632157L);

        TopicRecordFetchParams origin2 = TopicRecordFetchParams.byTimestamps(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.STRING,
                LIMIT,
                TIMEOUT_IN_MS,
                partitionTimestamp);

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json1 = mapper.writeValueAsString(origin1);
        Assert.assertNotNull(json1);

        String json2 = mapper.writeValueAsString(origin2);
        Assert.assertNotNull(json2);

        TopicRecordFetchParams deserialized1 = mapper.readValue(json1, TopicRecordFetchParams.class);
        Assert.assertNotNull(deserialized1);
        Assert.assertEquals(origin1, deserialized1);

        TopicRecordFetchParams deserialized2 = mapper.readValue(json2, TopicRecordFetchParams.class);
        Assert.assertNotNull(deserialized2);
        Assert.assertEquals(origin2, deserialized2);
    }

}
