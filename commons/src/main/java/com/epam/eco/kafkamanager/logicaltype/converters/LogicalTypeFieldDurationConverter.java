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
package com.epam.eco.kafkamanager.logicaltype.converters;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;

import com.epam.eco.kafkamanager.logicaltype.Duration;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeEnum;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeFieldAvroConverter;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeFieldDurationConverter implements LogicalTypeFieldAvroConverter<ByteBuffer, Duration> {

    private static final int DURATION_SIZE = 12;

    @Override
    public Duration convertInternal(
            Schema schema,
            @Nonnull ByteBuffer value
    ) {
        return new Duration(value.array());
    }

    @Override
    public Schema.Type getType() {
        return Schema.Type.FIXED;
    }

    @Override
    public LogicalTypeEnum getLocalType() {
        return LogicalTypeEnum.NONE;
    }

    @Override
    public boolean schemaAndLogicalTypeMatch(Schema schema, LogicalTypeEnum logicalType) {
         return schema.getType().equals(getType()) && schema.getFixedSize() == DURATION_SIZE;
    }

}
