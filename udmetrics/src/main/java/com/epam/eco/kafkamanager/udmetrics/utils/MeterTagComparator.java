/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.udmetrics.utils;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import io.micrometer.core.instrument.Tag;

/**
 * @author Andrei_Tytsik
 */
public class MeterTagComparator implements Comparator<Tag> {

    public static final MeterTagComparator INSTANCE = new MeterTagComparator();

    @Override
    public int compare(Tag o1, Tag o2) {
        if (o1 == o2) {
            return 0;
        } else if (o2 == null) {
            return 1;
        } else if (o1 == null) {
            return -1;
        }

        int result = ObjectUtils.compare(o1.getKey(), o2.getKey());
        if (result == 0) {
            result = ObjectUtils.compare(o1.getValue(), o2.getValue());
        }
        return result;
    }

}
