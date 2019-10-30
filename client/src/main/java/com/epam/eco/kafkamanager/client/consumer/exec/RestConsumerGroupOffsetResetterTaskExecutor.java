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
package com.epam.eco.kafkamanager.client.consumer.exec;

import java.util.Date;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.ConsumerGroupOffsetResetRequest;

/**
 * @author Raman_Babich
 */
public class RestConsumerGroupOffsetResetterTaskExecutor extends AbstractTaskExecutor<String, Map<TopicPartition, Long>, Void> implements ConsumerGroupOffsetResetterTaskExecutor {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public TaskResult<Void> doExecute(String resourceKey, Map<TopicPartition, Long> input) {
        Date start = new Date();
        try {
            ResponseEntity<TaskResult<Void>> response = restTemplate.exchange(
                    "/api/tasks/consumer-group-offset-resetter",
                    HttpMethod.POST,
                    new HttpEntity<>(new ConsumerGroupOffsetResetRequest(resourceKey, input)),
                    new ParameterizedTypeReference<TaskResult<Void>>() {
                    });
            return response.getBody();
        } catch (RestClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return TaskResult.<Void>builder()
                    .error(ex)
                    .startedAt(start)
                    .finishedAtNow()
                    .build();
        }
    }
}
