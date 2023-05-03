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
package com.epam.eco.kafkamanager.ui.browser;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.NoopRecordValueTabulator;

/**
 * @author Andrei_Tytsik
 */
public class NoopRecordValueTabulatorTest {

    @Test
    public void testNullIsTabulated() throws Exception {
        NoopRecordValueTabulator tabulator = new NoopRecordValueTabulator();

        Map<String, Object> tabularValue = tabulator.toTabularValue(createConsumerRecord(null));
        Assertions.assertNull(tabularValue);
    }

    @Test
    public void testObjectIsTabulated() throws Exception {
        NoopRecordValueTabulator tabulator = new NoopRecordValueTabulator();

        Object[] values = new Object[]{"stringvalue", new Object(), 1L, 1, 1f, 1d, false};
        for(Object value : values) {
            Map<String, Object> tabularValue = tabulator.toTabularValue(createConsumerRecord(value));
            Assertions.assertNotNull(tabularValue);
            Assertions.assertEquals(1, tabularValue.size());
            Assertions.assertTrue(tabularValue.containsKey(value.getClass().getSimpleName()));
            Assertions.assertEquals(value, tabularValue.get(value.getClass().getSimpleName()));
        }
    }

    private ConsumerRecord<?, Object> createConsumerRecord(Object value) {
        return new ConsumerRecord<Object, Object>("topic", 0, 0, null, value);
    }

}
