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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author Andrei_Tytsik
 */
public class NoopRecordValueTabulator implements RecordValueTabulator<Object> {

    @Override
    public Map<String, Object> toTabularValue(ConsumerRecord<?, Object> record) {
        Object value = record.value();
        return value != null ? Collections.singletonMap(columnName(value), value) : null;
    }

    @Override
    public Map<String, Object> getAttributes(ConsumerRecord<?, Object> record) {
        return null;
    }

    private String columnName(Object value) {
        return value.getClass().getSimpleName();
    }

}
