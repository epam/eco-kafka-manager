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
package com.epam.eco.kafkamanager.client.topic.exec;

import java.util.Date;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.epam.eco.kafkamanager.TopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.TopicRecordCountRequest;

/**
 * @author Raman_Babich
 */
public class RestTopicRecordCounterTaskExecutor extends AbstractAsyncStatefullTaskExecutor<String, Long> implements TopicRecordCounterTaskExecutor {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    public RestTopicRecordCounterTaskExecutor(CacheManager cacheManager) {
        super(cacheManager);
    }

    public RestTopicRecordCounterTaskExecutor(ExecutorService executor, CacheManager cacheManager) {
        super(executor, cacheManager);
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    @Override
    protected TaskResult<Long> doExecute(String resourceKey) {
        Date start = new Date();
        try {
            ResponseEntity<TaskResult<Long>> response = restTemplate.exchange(
                    "/api/tasks/topic-record-counter",
                    HttpMethod.POST,
                    new HttpEntity<>(new TopicRecordCountRequest(resourceKey)),
                    new ParameterizedTypeReference<TaskResult<Long>>() {
                    });
            return response.getBody();
        } catch (RestClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return TaskResult.<Long>builder()
                    .error(ex)
                    .startedAt(start)
                    .finishedAtNow()
                    .build();
        }
    }

}
