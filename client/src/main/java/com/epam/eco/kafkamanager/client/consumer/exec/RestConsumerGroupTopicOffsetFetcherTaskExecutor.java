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
package com.epam.eco.kafkamanager.client.consumer.exec;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.ConsumerGroupTopicOffsetFetchRequest;

/**
 * @author Raman_Babich
 */
public class RestConsumerGroupTopicOffsetFetcherTaskExecutor extends AbstractAsyncStatefullTaskExecutor<String, Map<TopicPartition, OffsetRange>> implements ConsumerGroupTopicOffsetFetcherTaskExecutor {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    public RestConsumerGroupTopicOffsetFetcherTaskExecutor(CacheManager cacheManager) {
        super(cacheManager);
    }

    public RestConsumerGroupTopicOffsetFetcherTaskExecutor(ExecutorService executor, CacheManager cacheManager) {
        super(executor, cacheManager);
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    @Override
    protected TaskResult<Map<TopicPartition, OffsetRange>> doExecute(String resourceKey) {
        Date start = new Date();
        try {
            ResponseEntity<TaskResult<Map<TopicPartition, OffsetRange>>> response = restTemplate.exchange(
                    "/api/tasks/consumer-group-topic-offset-fetcher",
                    HttpMethod.POST,
                    new HttpEntity<>(new ConsumerGroupTopicOffsetFetchRequest(resourceKey)),
                    new ParameterizedTypeReference<TaskResult<Map<TopicPartition, OffsetRange>>>() {});
            return response.getBody();
        } catch (RestClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return TaskResult.<Map<TopicPartition, OffsetRange>>builder()
                    .error(ex)
                    .startedAt(start)
                    .finishedAtNow()
                    .build();
        }
    }
}
