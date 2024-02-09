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
package com.epam.eco.kafkamanager.ui.browser;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseTombstonePredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_TOMBSTONES;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_EMPTY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateString;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseTombstonePredicate;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseTombstonePredicateTest {

    @Test
    public void testStringTombstonesExclude() {

        FilterClauseTombstonePredicate filterClausePredicate =
                getFilterClauseTombstonePredicate(FIELD_TOMBSTONES, FilterOperationEnum.EXCLUDE, FIELD_VALUE_EMPTY);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }

    @Test
    public void testStringTombstonesOnly() {

        FilterClauseTombstonePredicate filterClausePredicate =
                getFilterClauseTombstonePredicate(FIELD_TOMBSTONES, FilterOperationEnum.ONLY, FIELD_VALUE_EMPTY);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }


}