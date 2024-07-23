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

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import static org.apache.commons.collections4.MapUtils.isNotEmpty;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaKmProducer<K,V> {

    private static final String RESULT_STRING_FORMAT = "Successfully sent to the topic %s with key = <key>, partition = %d, offset = %d";
    private static final long PRODUCER_TIMEOUT_SEC = 10L;
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

        var record = new ProducerRecord<>(topicName, key, message);
        if(isNotEmpty(headers)) {
            populateHeaders(headers, record);
        }
        SendResult<K, V> sendResult = kafkaTemplate.send(record).get(PRODUCER_TIMEOUT_SEC, TimeUnit.SECONDS);

        return getResultString(sendResult);

    }

    private static <K, V> void populateHeaders(
            Map<String, String> headers,
            ProducerRecord<K, V> record
    ) {
         headers.keySet().forEach(
                 headerKey -> record.headers()
                         .add(headerKey, headers.get(headerKey).getBytes(StandardCharsets.UTF_8))
         );
    }

    private static <K, V> String getResultString(SendResult<K, V> sendResult) {
        RecordMetadata metadata = sendResult.getRecordMetadata();
        return String.format(RESULT_STRING_FORMAT, metadata.topic(), metadata.partition(), metadata.offset());
    }

}
