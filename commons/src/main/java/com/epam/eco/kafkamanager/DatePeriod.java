/*******************************************************************************
 *  Copyright 2022 EPAM Systems
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

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * @author Andrei_Tytsik
 */
public enum DatePeriod {

    ONE_HOUR(1, ChronoUnit.HOURS),
    TWO_HOURS(2, ChronoUnit.HOURS),
    THREE_HOURS(3, ChronoUnit.HOURS),

    ONE_DAY(1, ChronoUnit.DAYS),
    TWO_DAYS(2, ChronoUnit.DAYS),
    THREE_DAYS(3, ChronoUnit.DAYS),

    ONE_WEEK(1, ChronoUnit.WEEKS),
    TWO_WEEKS(2, ChronoUnit.WEEKS),
    THREE_WEEKS(3, ChronoUnit.WEEKS);

    private final long amount;
    private final TemporalUnit unit;

    DatePeriod(long amount, TemporalUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public long amount() {
        return amount;
    }
    public TemporalUnit unit() {
        return unit;
    }

}
