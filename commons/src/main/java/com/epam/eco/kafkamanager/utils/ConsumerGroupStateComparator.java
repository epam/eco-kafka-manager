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
package com.epam.eco.kafkamanager.utils;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.common.ConsumerGroupState;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupStateComparator implements Comparator<ConsumerGroupState> {

    public static final ConsumerGroupStateComparator INSTANCE = new ConsumerGroupStateComparator();

    @Override
    public int compare(ConsumerGroupState o1, ConsumerGroupState o2) {
        if (o1 == null || o2 == null) {
            return -1 * ObjectUtils.compare(o1, o2);
        }

        int result = weightFor(o1) - weightFor(o2);
        if (result == 0) {
            result = ObjectUtils.compare(o1.name(), o2.name());
        }
        return result;
    }

    private static int weightFor(ConsumerGroupState state) {
        switch (state) {
        case STABLE: return 0;
        case COMPLETING_REBALANCE: return 10;
        case PREPARING_REBALANCE: return 20;
        case EMPTY: return 30;
        case DEAD: return 40;
        case UNKNOWN: return 50;
        default: return 100;
        }
    }

}
