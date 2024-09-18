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

import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;

import com.epam.eco.kafkamanager.logicaltype.LogicalTypeEnum;
import com.epam.eco.kafkamanager.logicaltype.LogicalTypeFieldAvroConverter;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeFieldUuidConverter implements LogicalTypeFieldAvroConverter<String, UUID> {

    @Override
    public UUID convertInternal(
            Schema schema,
            @Nonnull String value
    ) {
        return UUID.fromString(value);
    }

    @Override
    public Schema.Type getType() {
        return Schema.Type.STRING;
    }

    @Override
    public LogicalTypeEnum getLocalType() {
        return LogicalTypeEnum.UUID;
    }

}
