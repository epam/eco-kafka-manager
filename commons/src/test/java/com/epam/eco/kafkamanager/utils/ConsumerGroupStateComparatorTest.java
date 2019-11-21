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
package com.epam.eco.kafkamanager.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.kafka.common.ConsumerGroupState;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupStateComparatorTest {

    @Test
    public void testOrdered() throws Exception {
        List<ConsumerGroupState> expected = Arrays.asList(
                ConsumerGroupState.STABLE,
                ConsumerGroupState.COMPLETING_REBALANCE,
                ConsumerGroupState.PREPARING_REBALANCE,
                ConsumerGroupState.EMPTY,
                ConsumerGroupState.DEAD,
                ConsumerGroupState.UNKNOWN,
                null);

        List<ConsumerGroupState> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);

        Collections.sort(actual, ConsumerGroupStateComparator.INSTANCE);
        Assert.assertEquals(expected, actual);
    }

}
