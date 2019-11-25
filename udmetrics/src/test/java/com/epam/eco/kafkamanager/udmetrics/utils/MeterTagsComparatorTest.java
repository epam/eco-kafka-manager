/*
 * Copyright 2019 EPAM Systems
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

import org.junit.Assert;
import org.junit.Test;

import io.micrometer.core.instrument.Tags;

/**
 * @author Andrei_Tytsik
 */
public class MeterTagsComparatorTest {

    @Test
    public void testCompared() throws Exception {
        Tags tags1 = null;
        Tags tags2 = null;
        Assert.assertEquals(0, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = Tags.of("a","a");
        tags2 = Tags.of("a","a");
        Assert.assertEquals(0, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = Tags.of("a","a");
        tags2 = null;
        Assert.assertEquals(1, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = null;
        tags2 = Tags.of("a","a");
        Assert.assertEquals(-1, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = Tags.of("a","a");
        tags2 = Tags.of("b","b");
        Assert.assertEquals(-1, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = Tags.of("b","b");
        tags2 = Tags.of("a","a");
        Assert.assertEquals(1, MeterTagsComparator.INSTANCE.compare(tags1, tags2));

        tags1 = Tags.of("a","a");
        tags2 = Tags.of("a","a", "c","c");
        Assert.assertEquals(-1, MeterTagsComparator.INSTANCE.compare(tags1, tags2));
    }

}
