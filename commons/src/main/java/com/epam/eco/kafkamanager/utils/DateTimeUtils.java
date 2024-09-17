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

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import com.epam.eco.kafkamanager.logicaltype.Duration;

/**
 * @author Mikhail_Vershkov
 */
public class DateTimeUtils {

    public static LocalDate intDateToLocalDate(int value) {
        return LocalDate.ofEpochDay(value);
    }

    public static int localDateToInt(LocalDate localDate) {
        return Long.valueOf(localDate.toEpochDay()).intValue();
    }

    public static LocalTime intToLocalTime(int value) {
        return LocalTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
    }

    public static LocalDateTime longToLocalDateTime(Long value) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
    }

    public static LocalDateTime longToLocalDateTimeWithMicros(long value) {
        Instant instant = Instant.ofEpochSecond(value/1_000_000)
                .plusNanos((value%1_000_000)*1000)
                .truncatedTo(ChronoUnit.MICROS);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static long localDateTimeToLongWithMicros(LocalDateTime localDateTime) {
       Instant instant = localDateTime.atZone(ZoneOffset.UTC).toInstant();
       return instant.getEpochSecond()*1_000_000 + instant.getNano()/1_000;
    }

    public static LocalTime longToLocalTimeWithMicros(long value) {
        Instant instant = Instant.ofEpochSecond(value/1_000_000)
                .plusNanos((value%1_000_000)*1000)
                .truncatedTo(ChronoUnit.MICROS);
        return LocalTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static long localTimeToLongWithMicros(LocalTime localTime) {
        Instant instant = localTime.atDate(LocalDate.now()).toInstant(ZoneOffset.UTC);
        return instant.getEpochSecond()*1_000_000 + instant.getNano()/1_000;
    }

    public static long localTimeToLongWithMillis(LocalTime localTime) {
        return localTime.getLong(ChronoField.SECOND_OF_DAY) * 1_000 + localTime.getNano()/1_000_000;
    }

    public static int intFromByteArray(byte[] array, int position) {
        if((position+3)>array.length-1) {
            throw new IllegalArgumentException("Maximum byte position (" + (position+3) + ") is greater than array length");
        }
        return ByteBuffer.wrap(Arrays.copyOfRange(array, position, position+4)).getInt();
    }

    public static byte[] byteArrayFormDuration(Duration duration) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(0, duration.getMonths());
        buffer.putInt(4, duration.getDays());
        buffer.putInt(8, duration.getMillis());
        return buffer.array();
    }

    public static Duration durationFromByteArray(ByteBuffer buffer) {
        return new Duration(buffer.getInt(0), buffer.getInt(4), buffer.getInt(5)) ;
    }

}
