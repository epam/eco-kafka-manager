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
package com.epam.eco.kafkamanager.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeConverterUtils {

    public static final String TIMESTAMP_MILLIS_FIELD_NAME = "timestampmillisfield";
    public static final String TIMESTAMP_MICROS_FIELD_NAME = "timestampmicrosfield";
    public static final String TIME_MILLIS_FIELD_NAME = "timemillisfield";
    public static final String TIME_MICROS_FIELD_NAME = "timemicrosfield";
    public static final String DATE_FIELD_NAME = "datefield";
    public static final String DECIMAL_FIELD_NAME = "decimalfield";
    public static final String DURATION_FIELD_NAME = "durationfield";
    public static final String UUID_FIELD_NAME = "uuidfield";
    public static final String INT_FIELD_NAME = "intfield";

    private static final String TIMESTAMP_MILLIS_SCHEMA = "[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}]";
    private static final String TIMESTAMP_MICROS_SCHEMA = "[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-micros\"}]";

    private static final String TIME_MILLIS_SCHEMA = "[\"null\",{\"type\":\"int\",\"logicalType\":\"time-millis\"}]";
    private static final String TIME_MICROS_SCHEMA = "[\"null\",{\"type\":\"long\",\"logicalType\":\"time-micros\"}]";

    private static final String DATE_INT_SCHEMA = "[\"null\",{\"type\":\"int\",\"logicalType\":\"date\"}]";
    private static final String BYTES_DECIMAL_SCHEMA = "[\"null\",{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":5,\"scale\":2}]";

    private static final String DURATION_SCHEMA = "[\"null\",{\"type\":\"fixed\",\"name\":\"test\",\"size\":12}]";

    private static final String UUID_SCHEMA = "[\"null\",{\"type\":\"string\",\"logicalType\":\"uuid\"}]";

    private static final String INT_SCHEMA = "[\"null\",{\"type\":\"int\",\"name\":\"testInt\"}]";

    private static final String TEST_SCHEMA = "{\n" +
            "\"type\": \"record\",\n" +
            "\"name\": \"test_name\",\n" +
            "\"fields\" : [\n" +
            "\t      {\"name\": \"" + TIMESTAMP_MILLIS_FIELD_NAME + "\", \"type\": " + TIMESTAMP_MILLIS_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + TIMESTAMP_MICROS_FIELD_NAME + "\", \"type\": " + TIMESTAMP_MICROS_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + TIME_MILLIS_FIELD_NAME + "\", \"type\": " + TIME_MILLIS_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + TIME_MICROS_FIELD_NAME + "\", \"type\": " + TIME_MICROS_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + DATE_FIELD_NAME + "\", \"type\": " + DATE_INT_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + DECIMAL_FIELD_NAME + "\", \"type\": " + BYTES_DECIMAL_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + DURATION_FIELD_NAME + "\", \"type\": " + DURATION_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + UUID_FIELD_NAME + "\", \"type\": " + UUID_SCHEMA + "},\n" +
            "\t      {\"name\": \"" + INT_FIELD_NAME + "\", \"type\": " + INT_SCHEMA + "}\n" +
            " ]\n" +
            "}";


    public static Schema getTimestampMillisSchema() {
        return new Schema.Parser().parse(TIMESTAMP_MILLIS_SCHEMA);
    }
    public static Schema getTimestampMicrosSchema() {
        return new Schema.Parser().parse(TIMESTAMP_MICROS_SCHEMA);
    }

    public static Schema getTimeMillisSchema() {
        return new Schema.Parser().parse(TIME_MILLIS_SCHEMA);
    }
    public static Schema getTimeMicrosSchema() {
        return new Schema.Parser().parse(TIME_MICROS_SCHEMA);
    }

    public static Schema getIntDateSchema() {
        return new Schema.Parser().parse(DATE_INT_SCHEMA);
    }
    public static Schema getBytesDecimalSchema() {
        return new Schema.Parser().parse(BYTES_DECIMAL_SCHEMA);
    }

    public static Schema getDurationSchema() {
        return new Schema.Parser().parse(DURATION_SCHEMA);
    }

    public static Schema getUuidSchema() {
        return new Schema.Parser().parse(UUID_SCHEMA);
    }

    public static Schema getTestSchema() {
        return new Schema.Parser().parse(TEST_SCHEMA);
    }

    public static ByteBuffer longToByteBuffer(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer;
    }

    public static GenericRecord createNewRecord() {
        return new GenericData.Record(getTestSchema());
    }

    public static Schema schemaFromResource(String path) {
        Validate.notBlank(path, "path is blank", new Object[0]);

        try (InputStream inputStream = LogicalTypeConverterUtils.class.getResourceAsStream(path)) {
            return (new Schema.Parser()).parse(inputStream);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
