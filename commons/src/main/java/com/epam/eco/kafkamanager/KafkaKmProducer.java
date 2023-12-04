/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaKmProducer<K,V> {

    private static final String RESULT_STRING_FORMAT = "Successfully sent to the topic %s with key = %s";
    private static final long PRODUCER_TIMEOUT = 10L;
    private final KafkaTemplate<K, V> kafkaTemplate;

    @Autowired
    public KafkaKmProducer(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String send(String topicName,
                       K key,
                       V message,
                       Map<String, String> headers) throws ExecutionException, InterruptedException, TimeoutException {

        Validate.notNull(topicName,"Topic is null!");
        Validate.notNull(key,"Key is null!");

        var record = new ProducerRecord<K,V>(topicName, key, message);

        if(MapUtils.isNotEmpty(headers)) {
            headers.keySet().forEach(headerKey -> record.headers().add(headerKey, headers.get(headerKey).getBytes(
                    StandardCharsets.UTF_8)));
        }

        kafkaTemplate.send(record).get(PRODUCER_TIMEOUT, TimeUnit.SECONDS);

        return String.format(RESULT_STRING_FORMAT, topicName, key);

    }
}
