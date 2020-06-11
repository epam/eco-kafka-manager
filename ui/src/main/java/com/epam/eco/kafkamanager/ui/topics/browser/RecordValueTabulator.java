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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author Andrei_Tytsik
 */
public interface RecordValueTabulator<V> {
    Map<String, Object> toTabularValue(ConsumerRecord<?, V> record);
    Map<String, Object> getAttributes(ConsumerRecord<?, V> record);
}
