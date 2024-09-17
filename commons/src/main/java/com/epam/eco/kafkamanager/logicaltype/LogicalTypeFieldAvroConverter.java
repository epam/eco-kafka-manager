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

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */
public interface LogicalTypeFieldAvroConverter<K,V> {

    V convertInternal(Schema schema, K value);

    default V convert(Schema schema, K value) {
        return isNull(value) ? null : convertInternal(schema, value);
    }

    Schema.Type getType();

    LogicalTypeEnum getLocalType();

    default boolean match(
            Schema schema,
            LogicalType logicalType
    ) {
        return schema.getType().equals(Schema.Type.UNION) ?
               schema.getTypes().stream()
                       .anyMatch(fieldSchema->
                            schemaAndLogicalTypeMatch(fieldSchema, getLogicalTypeEnum(fieldSchema))) :
               schemaAndLogicalTypeMatch(schema, LogicalTypeEnum.valueOrNone(logicalType));
    }

    private static LogicalTypeEnum getLogicalTypeEnum(Schema schema) {
        if(isNull(schema) || isNull(schema.getLogicalType())) {
            return LogicalTypeEnum.NONE;
        }
        return LogicalTypeEnum.valueOrNone(schema.getLogicalType());
    }

    default boolean schemaAndLogicalTypeMatch(Schema schema, LogicalTypeEnum logicalType) {
        if(isNull(logicalType)) {
            return false;
        }
        return getType().equals(schema.getType()) && logicalType == getLocalType();
    }
 }
