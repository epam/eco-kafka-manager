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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

/**
 * @author Andrei_Tytsik
 */
public class MeterTagsComparator implements Comparator<Tags> {

    public static final MeterTagsComparator INSTANCE = new MeterTagsComparator();

    @Override
    public int compare(Tags o1, Tags o2) {
        if (o1 == o2) {
            return 0;
        } else if (o2 == null) {
            return 1;
        } else if (o1 == null) {
            return -1;
        }

        List<Tag> list1 = asList(o1);
        List<Tag> list2 = asList(o2);
        int result = 0;
        for (int i = 0; result == 0 && i < Math.max(list1.size(), list2.size()); i++) {
            Tag t1 = i < list1.size() ? list1.get(i) : null;
            Tag t2 = i < list2.size() ? list2.get(i) : null;
            result = MeterTagComparator.INSTANCE.compare(t1, t2);
        }
        return result;
    }

    private static List<Tag> asList(Tags tags) {
        List<Tag> list = new ArrayList<>();
        tags.forEach(list::add);
        return list;
    }

}
