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
package com.epam.eco.kafkamanager.logicaltype;

import java.util.List;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldByteBufferToDecimalConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldDurationConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldIntDateConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldOtherAvroConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldTimeMicrosConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldTimeMillisConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldTimestampMicrosConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldTimestampMillisConverter;
import com.epam.eco.kafkamanager.logicaltype.converters.LogicalTypeFieldUuidConverter;


/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeFieldConverter {

    private static final List<LogicalTypeFieldAvroConverter> CONVERTERS = List.of(
         new LogicalTypeFieldTimestampMillisConverter(),
         new LogicalTypeFieldTimestampMicrosConverter(),
         new LogicalTypeFieldTimeMillisConverter(),
         new LogicalTypeFieldTimeMicrosConverter(),
         new LogicalTypeFieldByteBufferToDecimalConverter(),
         new LogicalTypeFieldIntDateConverter(),
         new LogicalTypeFieldDurationConverter(),
         new LogicalTypeFieldUuidConverter()
    );

    public static Object convert(Schema schema,
                                 LogicalType logicalType,
                                 Object value
    ) {
        return CONVERTERS.stream()
                .filter(converter->converter.match(schema, logicalType))
                .findFirst()
                .orElse(new LogicalTypeFieldOtherAvroConverter())
                .convert(schema, value);
    }
}
