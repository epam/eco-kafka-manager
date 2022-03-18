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
package com.epam.eco.kafkamanager.udmetrics.utils;

import java.util.Comparator;

import com.epam.eco.kafkamanager.udmetrics.Metric;

/**
 * @author Andrei_Tytsik
 */
public class MetricComparator implements Comparator<Metric> {

    public static final MetricComparator INSTANCE = new MetricComparator();

    @Override
    public int compare(Metric o1, Metric o2) {
        if (o1 == o2) {
            return 0;
        } else if (o2 == null) {
            return 1;
        } else if (o1 == null) {
            return -1;
        }

        return MeterTagsComparator.INSTANCE.compare(o1.getTags(), o2.getTags());
    }

}
