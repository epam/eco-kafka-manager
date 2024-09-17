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

import static com.epam.eco.kafkamanager.utils.DateTimeUtils.intFromByteArray;

/**
 * @author Mikhail_Vershkov
 */
public class Duration {

    private static final int MONTH_POSITION = 0;
    private static final int DAY_POSITION = 4;
    private static final int MILLIS_POSITION = 8;

    private final int months;
    private final int days;
    private final int millis;

    public Duration(int months, int days, int millis) {
        this.months = months;
        this.days = days;
        this.millis = millis;
    }
    public Duration(byte[] bytes) {
        this.months = intFromByteArray(bytes, MONTH_POSITION);
        this.days = intFromByteArray(bytes, DAY_POSITION);
        this.millis = intFromByteArray(bytes, MILLIS_POSITION);
    }
    public int getMonths() {
        return months;
    }
    public int getDays() {
        return days;
    }
    public int getMillis() {
        return millis;
    }

    @Override
    public final boolean equals(Object o) {
        if(this == o)
            return true;
        if(!(o instanceof Duration duration))
            return false;

        return months == duration.months && days == duration.days && millis == duration.millis;
    }

    @Override
    public int hashCode() {
        int result = months;
        result = 31 * result + days;
        result = 31 * result + millis;
        return result;
    }

    @Override
    public String toString() {
        return String.format("Months: %d, days: %d, millis: %d", months, days, millis);
    }
}
