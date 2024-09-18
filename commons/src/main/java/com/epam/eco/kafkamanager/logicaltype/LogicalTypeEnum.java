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

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.avro.LogicalType;

/**
 * @author Mikhail_Vershkov
 */
public enum LogicalTypeEnum {

    DATE(new LogicalType("date")),
    TIMESTAMP_MILLIS(new LogicalType("timestamp-millis")),
    TIMESTAMP_MICROS(new LogicalType("timestamp-micros")),
    TIME_MILLIS(new LogicalType("time-millis")),
    TIME_MICROS(new LogicalType("time-micros")),
    DURATION(new LogicalType("duration")),
    DECIMAL(new LogicalType("decimal")),
    UUID(new LogicalType("uuid")),
    NONE(new LogicalType("none"));

    private final LogicalType value;

    LogicalTypeEnum(LogicalType value) {
        this.value = value;
    }
    public LogicalType getValue() {
        return value;
    }
    public static LogicalTypeEnum valueOrNone(@Nonnull LogicalType value) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().getName().equals(value.getName()))
                .findFirst()
                .orElse(NONE);
    }
}
