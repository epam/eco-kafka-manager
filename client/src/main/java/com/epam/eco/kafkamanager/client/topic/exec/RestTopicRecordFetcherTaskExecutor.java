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
package com.epam.eco.kafkamanager.client.topic.exec;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.TopicRecordFetchRequest;

/**
 * @author Raman_Babich
 */
public class RestTopicRecordFetcherTaskExecutor<K, V> extends AbstractTaskExecutor<String, TopicRecordFetchParams, RecordFetchResult<K, V>> implements TopicRecordFetcherTaskExecutor<K, V> {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    @Override
    protected TaskResult<RecordFetchResult<K, V>> doExecute(String resourceKey, TopicRecordFetchParams input) {
        Date start = new Date();
        try {
            ResponseEntity<TaskResult<RecordFetchResult<K, V>>> response = restTemplate.exchange(
                    "/api/tasks/topic-record-fetcher",
                    HttpMethod.POST,
                    new HttpEntity<>(new TopicRecordFetchRequest(resourceKey, input)),
                    new ParameterizedTypeReference<TaskResult<RecordFetchResult<K, V>>>() {
                    });
            return response.getBody();
        } catch (RestClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return TaskResult.<RecordFetchResult<K, V>>builder()
                    .error(ex)
                    .startedAt(start)
                    .finishedAtNow()
                    .build();
        }
    }

}
