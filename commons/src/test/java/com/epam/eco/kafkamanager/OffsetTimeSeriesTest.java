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

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class OffsetTimeSeriesTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        OffsetTimeSeries origin = new OffsetTimeSeries(new TopicPartition("topic", 0));
        origin.append(LocalDateTime.now(), 42l);
        origin.append(LocalDateTime.now(), 43l);
        origin.append(LocalDateTime.now(), 44l);

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        OffsetTimeSeries deserialized = mapper.readValue(json, OffsetTimeSeries.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeltaCaclulated() throws Exception {
        OffsetTimeSeries series = new OffsetTimeSeries(new TopicPartition("topic", 0));

        LocalDateTime dateTime = LocalDateTime.now();

        series.append(dateTime.minus(1, series.getGranularity()), 1l);
        series.append(dateTime, 100l);

        Assertions.assertEquals(Long.valueOf(99), series.deltaAtDate(dateTime));
    }

    @Test
    public void testRateCaclulated() throws Exception {
        OffsetTimeSeries series = new OffsetTimeSeries(new TopicPartition("topic", 0));

        LocalDateTime date = LocalDateTime.now();

        // serial dates
        LocalDateTime date1 = date.minus(2, series.getGranularity());
        LocalDateTime date2 = date.minus(1, series.getGranularity());
        LocalDateTime date3 = date;

        series.append(date1, 0l);
        series.append(date2, 120l);
        series.append(date3, 200l);

        Assertions.assertNotNull(series.currentRatePerSec());
        Assertions.assertNotNull(series.currentRatePerMinute());
        Assertions.assertNotNull(series.currentRatePerHour());

        series = new OffsetTimeSeries(new TopicPartition("topic", 0));

        date = LocalDateTime.now();

        // non-serial dates
        date1 = date.minus(9, series.getGranularity());
        date2 = date.minus(5, series.getGranularity());
        date3 = date;

        series.append(date1, 0l);
        series.append(date2, 120l);
        series.append(date3, 200l);

        Assertions.assertNotNull(series.currentRatePerSec());
        Assertions.assertNotNull(series.currentRatePerMinute());
        Assertions.assertNotNull(series.currentRatePerHour());
    }

}
