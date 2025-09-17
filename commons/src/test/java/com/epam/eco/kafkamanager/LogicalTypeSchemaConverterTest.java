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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.logicaltype.Duration;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeSchemaConverter;
import com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils;

import static com.epam.eco.kafkamanager.utils.DateTimeUtils.byteArrayFormDuration;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localDateTimeToLongWithMicros;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localDateToInt;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localTimeToLongWithMicros;
import static com.epam.eco.kafkamanager.utils.DateTimeUtils.localTimeToLongWithMillis;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.DATE_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.DECIMAL_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.DURATION_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.INT_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.TIMESTAMP_MICROS_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.TIMESTAMP_MILLIS_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.TIME_MICROS_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.TIME_MILLIS_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.UUID_FIELD_NAME;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.createNewRecord;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.longToByteBuffer;
import static com.epam.eco.kafkamanager.utils.LogicalTypeConverterUtils.schemaFromResource;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeSchemaConverterTest {

    private static final long DECIMAL_TEST_VALUE = 123456L;

    @Test
    public void testConvertLogicalTimestampType() {

        LocalDateTime now = LocalDateTime.now();
        GenericRecord record =  createNewRecord();

        record.put(TIMESTAMP_MILLIS_FIELD_NAME, now.toInstant(ZoneOffset.UTC).toEpochMilli());
        record.put(TIMESTAMP_MICROS_FIELD_NAME, localDateTimeToLongWithMicros(now));

        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record) ;

        Assertions.assertInstanceOf(LocalDateTime.class, map.get(TIMESTAMP_MILLIS_FIELD_NAME));
        assertEquals(now.truncatedTo(ChronoUnit.MILLIS), map.get(TIMESTAMP_MILLIS_FIELD_NAME));

        Assertions.assertInstanceOf(LocalDateTime.class, map.get(TIMESTAMP_MICROS_FIELD_NAME));
        assertEquals(now.truncatedTo(ChronoUnit.MICROS), map.get(TIMESTAMP_MICROS_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalTimestampTypeIfNull() {
        GenericRecord record =  createNewRecord();
        record.put(TIMESTAMP_MILLIS_FIELD_NAME, null);
        record.put(TIMESTAMP_MICROS_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record) ;
        Assertions.assertNull(map.get(TIMESTAMP_MILLIS_FIELD_NAME));
        Assertions.assertNull(map.get(TIMESTAMP_MICROS_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalTimeType() {
        LocalTime time = LocalTime.now();
        GenericRecord record =  createNewRecord();
        record.put(TIME_MILLIS_FIELD_NAME, Long.valueOf(localTimeToLongWithMillis(time)).intValue());
        record.put(TIME_MICROS_FIELD_NAME, localTimeToLongWithMicros(time));
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertInstanceOf(LocalTime.class, map.get(TIME_MILLIS_FIELD_NAME));
        assertEquals(time.truncatedTo(ChronoUnit.MILLIS), map.get(TIME_MILLIS_FIELD_NAME));
        Assertions.assertInstanceOf(LocalTime.class, map.get(TIME_MICROS_FIELD_NAME));
        assertEquals(time.truncatedTo(ChronoUnit.MICROS), map.get(TIME_MICROS_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalTimeTypeIfNull() {
        LocalTime time = LocalTime.now();
        GenericRecord record =  createNewRecord();
        record.put(TIME_MILLIS_FIELD_NAME, null);
        record.put(TIME_MICROS_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(TIME_MILLIS_FIELD_NAME));
        Assertions.assertNull(map.get(TIME_MICROS_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalDateType() {
        LocalDate date = LocalDate.now();
        GenericRecord record =  createNewRecord();
        record.put(DATE_FIELD_NAME, localDateToInt(date));
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertInstanceOf(LocalDate.class, map.get(DATE_FIELD_NAME));
        assertEquals(date, map.get(DATE_FIELD_NAME));
    }
    @Test
    public void testConvertLogicalDateTypeIfNull() {
        LocalDate date = LocalDate.now();
        GenericRecord record =  createNewRecord();
        record.put(DATE_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(DATE_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalDecimalType() {
        GenericRecord record =  createNewRecord();
        record.put(DECIMAL_FIELD_NAME, longToByteBuffer(DECIMAL_TEST_VALUE));
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertInstanceOf(BigDecimal.class, map.get(DECIMAL_FIELD_NAME));
        ByteBuffer buffer = longToByteBuffer(DECIMAL_TEST_VALUE);
        BigDecimal expected = new BigDecimal(new BigInteger(buffer.array()), 2); // Scale is 2 from schema
        assertEquals(expected, map.get(DECIMAL_FIELD_NAME));
    }
    @Test
    public void testConvertLogicalDecimalTypeIfNull() {
        GenericRecord record =  createNewRecord();
        record.put(DECIMAL_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(DECIMAL_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalDurationType() {
        Duration duration = new Duration(12,34,567);
        GenericRecord record =  createNewRecord();
        record.put(DURATION_FIELD_NAME, ByteBuffer.wrap(byteArrayFormDuration(duration)));
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertInstanceOf(Duration.class, map.get(DURATION_FIELD_NAME));
        assertEquals(duration, map.get(DURATION_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalDurationTypeIfNull() {
        GenericRecord record =  createNewRecord();
        record.put(DURATION_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(DURATION_FIELD_NAME));
    }
    @Test
    public void testConvertLogicalDurationTypeIfEmpty() {
        GenericRecord record =  createNewRecord();
        record.put(DURATION_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(DURATION_FIELD_NAME));
    }

    @Test
    public void testConvertLogicalUuidType() {
        UUID uuid = UUID.randomUUID();
        GenericRecord record =  createNewRecord();
        record.put(UUID_FIELD_NAME, uuid.toString());
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertInstanceOf(UUID.class, map.get(UUID_FIELD_NAME));
        assertEquals(uuid, map.get(UUID_FIELD_NAME));
    }
    @Test
    public void testConvertLogicalUuidTypeIfNull() {
        GenericRecord record =  createNewRecord();
        record.put(UUID_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(UUID_FIELD_NAME));
    }

    @Test
    public void testConvertIntType_IfNull() {
        GenericRecord record =  createNewRecord();
        record.put(INT_FIELD_NAME, null);
        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);
        Assertions.assertNull(map.get(INT_FIELD_NAME));
    }

    @Test
    void testUnionConvertFirstType() {
        GenericRecord record = new GenericData.Record(schemaFromResource(
                "/schemas/test_union_record.avsc")
        );
        GenericRecord subRecord =
                new GenericData.Record(record.getSchema().getField("attributes")
                        .schema()
                        .getTypes()
                        .get(1)
                );
        subRecord.put("project", "a");
        subRecord.put("service", "b");
        subRecord.put("namespace", "c");
        record.put("attributes", subRecord);

        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);

        Map<String, String> actualAttributes = (Map<String, String>) map.get("attributes");
        assertEquals(subRecord.getSchema().getFields().size(),
                actualAttributes.size());
        assertEquals("a", actualAttributes.get("project"));
        assertEquals("b", actualAttributes.get("service"));
        assertEquals("c", actualAttributes.get("namespace"));
    }

    @Test
    void testUnionConvertSecondType() {
        GenericRecord record = new GenericData.Record(schemaFromResource(
                "/schemas/test_union_record.avsc")
        );
        GenericRecord subRecord =
                new GenericData.Record(record.getSchema().getField("attributes")
                        .schema()
                        .getTypes()
                        .get(2)
                );
        subRecord.put("user", "d");
        subRecord.put("work", "e");
        subRecord.put("station", "f");
        record.put("attributes", subRecord);

        Map<String, Object> map = LogicalTypeSchemaConverter.convert(record);

        Map<String, String> actualAttributes = (Map<String, String>) map.get("attributes");
        assertEquals(subRecord.getSchema().getFields().size(), actualAttributes.size());
        assertEquals("d", actualAttributes.get("user"));
        assertEquals("e", actualAttributes.get("work"));
        assertEquals("f", actualAttributes.get("station"));
    }

}
