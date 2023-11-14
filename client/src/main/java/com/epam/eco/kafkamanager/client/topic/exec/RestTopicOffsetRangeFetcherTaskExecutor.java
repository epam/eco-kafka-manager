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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import jakarta.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.TopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.TopicOffsetRangeFetchRequest;

/**
 * @author Raman_Babich
 */
@SuppressWarnings("deprecation")
public class RestTopicOffsetRangeFetcherTaskExecutor extends
        AbstractAsyncStatefullTaskExecutor<String, Map<TopicPartition, OffsetRange>> implements
        TopicOffsetRangeFetcherTaskExecutor, TopicOffsetFetcherTaskExecutor {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    public RestTopicOffsetRangeFetcherTaskExecutor(CacheManager cacheManager) {
        super(cacheManager);
    }

    public RestTopicOffsetRangeFetcherTaskExecutor(ExecutorService executor, CacheManager cacheManager) {
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
                    "/api/tasks/topic-offset-range-fetcher",
                    HttpMethod.POST,
                    new HttpEntity<>(new TopicOffsetRangeFetchRequest(resourceKey)),
                    new ParameterizedTypeReference<>() {});
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

    @Override
    public Map<TopicPartition, OffsetTimeSeries> getOffsetTimeSeries(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("topicName", topicName);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("/api/tasks/topic-offset-range-fetcher/{topicName}")
                .uriVariables(uriVariables);

        ResponseEntity<Map<TopicPartition, OffsetTimeSeries>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>(){});

        Map<TopicPartition, OffsetTimeSeries> responseMap = response.getBody();
        return responseMap != null ? responseMap : Collections.emptyMap();
    }

}
