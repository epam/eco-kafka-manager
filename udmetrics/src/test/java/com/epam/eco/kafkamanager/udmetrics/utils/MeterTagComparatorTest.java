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

import org.junit.Assert;
import org.junit.Test;

import io.micrometer.core.instrument.Tag;


/**
 * @author Andrei_Tytsik
 */
public class MeterTagComparatorTest {

    @Test
    public void testCompared() throws Exception {
        Assert.assertEquals(0, MeterTagComparator.INSTANCE.compare(null, null));
        Assert.assertEquals(0, MeterTagComparator.INSTANCE.compare(Tag.of("a", "a"), Tag.of("a", "a")));
        Assert.assertEquals(1, MeterTagComparator.INSTANCE.compare(Tag.of("a", "a"), null));
        Assert.assertEquals(-1, MeterTagComparator.INSTANCE.compare(null, Tag.of("a", "a")));
        Assert.assertEquals(1, MeterTagComparator.INSTANCE.compare(Tag.of("b", "b"), Tag.of("a", "a")));
        Assert.assertEquals(-1, MeterTagComparator.INSTANCE.compare(Tag.of("a", "a"), Tag.of("b", "b")));
    }

}
