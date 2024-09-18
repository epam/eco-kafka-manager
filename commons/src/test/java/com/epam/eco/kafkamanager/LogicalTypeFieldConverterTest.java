/*******************************************************************************
 *  Copyright 2024 EPAM Systems
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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.logicaltype.Duration;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeEnum;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeFieldConverter;

import static com.epam.eco.kafkamanager.utils.DateTimeUtils.byteArrayFormDuration;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localDateToInt;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getBytesDecimalSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getDurationSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getIntDateSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getTimeMicrosSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getTimeMillisSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getTimestampMicrosSchema;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getTimestampMillisSchema;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localDateTimeToLongWithMicros;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localTimeToLongWithMicros;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.getUuidSchema;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeFieldConverterTest {


    @Test
    public void timestampMillisTest() {
        Schema schema = getTimestampMillisSchema();
        LogicalType logicalType = LogicalTypeEnum.TIMESTAMP_MILLIS.getValue();
        LocalDateTime testDateTime = LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.MILLIS);
        Long value = testDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, value);
        Assertions.assertInstanceOf(LocalDateTime.class, result);
        LocalDateTime resultDateTime = (LocalDateTime) result;
        Assertions.assertEquals(testDateTime,resultDateTime);
    }

    @Test
    public void timestampMicrosTest() {
        Schema schema = getTimestampMicrosSchema();
        LogicalType logicalType = LogicalTypeEnum.TIMESTAMP_MICROS.getValue();
        LocalDateTime testDateTime = LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.MICROS);
        Long value = localDateTimeToLongWithMicros(testDateTime);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, value);
        Assertions.assertInstanceOf(LocalDateTime.class, result);
        LocalDateTime resultDateTime = (LocalDateTime) result;
        Assertions.assertEquals(testDateTime,resultDateTime);
    }

    @Test
    public void timeMillisTest() {
        Schema schema = getTimeMillisSchema();
        LogicalType logicalType = LogicalTypeEnum.TIME_MILLIS.getValue();
        LocalTime testTime = LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .toLocalTime()
                .truncatedTo(ChronoUnit.MILLIS);
        int value = testTime.get(ChronoField.MILLI_OF_DAY);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, value);
        Assertions.assertInstanceOf(LocalTime.class, result);
        LocalTime resultTime = (LocalTime) result;
        Assertions.assertEquals(testTime,resultTime);
    }

    @Test
    public void timeMicrosTest() {
        Schema schema = getTimeMicrosSchema();
        LogicalType logicalType = LogicalTypeEnum.TIME_MICROS.getValue();
        LocalTime testTime = LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .toLocalTime()
                .truncatedTo(ChronoUnit.MICROS);
        Long value = localTimeToLongWithMicros(testTime);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, value);
        Assertions.assertInstanceOf(LocalTime.class, result);
        LocalTime resultTime = (LocalTime) result;
        Assertions.assertEquals(testTime,resultTime);
    }

    @Test
    public void dateIntTest() {
        Schema schema = getIntDateSchema();
        LogicalType logicalType = LogicalTypeEnum.DATE.getValue();
        LocalDate testDate = LocalDate.now();
        int value = localDateToInt(testDate);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, value);
        Assertions.assertInstanceOf(LocalDate.class, result);
        LocalDate resultDate = (LocalDate) result;
        Assertions.assertEquals(testDate, resultDate);
    }

    @Test
    public void dateByteToDecimalTest() {
        Schema schema = getBytesDecimalSchema();
        LogicalType logicalType = LogicalTypeEnum.DECIMAL.getValue();
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,8,9,0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, buffer);
        Assertions.assertInstanceOf(Long.class, result);
        Long resultLong = (Long) result;
        Assertions.assertEquals(new BigInteger(bytes).longValue(), resultLong);
    }

    @Test
    public void dateByteToDurationTest() {
        Schema schema = getDurationSchema();
        LogicalType logicalType = LogicalTypeEnum.DURATION.getValue();
        Duration testDuration = new Duration(123, 456, 789);
        byte[] bytes = byteArrayFormDuration(testDuration);
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, ByteBuffer.wrap(bytes));
        Assertions.assertInstanceOf(Duration.class, result);
        Duration duration = (Duration) result;
        Assertions.assertEquals(duration, testDuration);
    }

    @Test
    public void stringToUuidTest() {
        Schema schema = getUuidSchema();
        LogicalType logicalType = LogicalTypeEnum.UUID.getValue();
        UUID uuid = UUID.randomUUID();
        Object result = LogicalTypeFieldConverter.convert(schema, logicalType, uuid.toString());
        Assertions.assertInstanceOf(UUID.class, result);
        Assertions.assertEquals(uuid, result);
    }

}
