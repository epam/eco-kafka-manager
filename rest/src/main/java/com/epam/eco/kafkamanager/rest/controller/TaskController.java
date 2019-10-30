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
package com.epam.eco.kafkamanager.rest.controller;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.config.KafkaManagerRestProperties;
import com.epam.eco.kafkamanager.rest.request.ConsumerGroupOffsetResetRequest;
import com.epam.eco.kafkamanager.rest.request.ConsumerGroupTopicOffsetFetchRequest;
import com.epam.eco.kafkamanager.rest.request.TopicOffsetFetchRequest;
import com.epam.eco.kafkamanager.rest.request.TopicPurgeRequest;
import com.epam.eco.kafkamanager.rest.request.TopicRecordCountRequest;
import com.epam.eco.kafkamanager.rest.request.TopicRecordFetchRequest;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private KafkaManager kafkaManager;

    @Autowired
    private KafkaManagerRestProperties properties;

    @PostMapping("/topic-record-counter")
    public DeferredResult<TaskResult<Long>> topicRecordCounter(
            @RequestBody TopicRecordCountRequest request) {
        DeferredResult<TaskResult<Long>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<Long> result = kafkaManager.getTopicRecordCounterTaskExecutor()
                        .executeDetailed(request.getTopicName());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

    @PostMapping("/topic-offset-fetcher")
    public DeferredResult<TaskResult<Map<TopicPartition, OffsetRange>>> topicOffsetFetch(
            @RequestBody TopicOffsetFetchRequest request) {
        DeferredResult<TaskResult<Map<TopicPartition, OffsetRange>>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<Map<TopicPartition, OffsetRange>> result =
                        kafkaManager.getTopicOffsetFetcherTaskExecutor()
                        .executeDetailed(request.getTopicName());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

    @GetMapping("/topic-offset-fetcher/{topicName}")
    public Map<TopicPartition, OffsetTimeSeries> topicOffsetFetcher(
            @PathVariable("topicName") String topicName) {
        return kafkaManager.getTopicOffsetFetcherTaskExecutor().getOffsetTimeSeries(topicName);
    }

    @PostMapping("/topic-record-fetcher")
    public DeferredResult<TaskResult<RecordFetchResult<Object, Object>>> topicRecordFetcher(
            @RequestBody TopicRecordFetchRequest request) {
        DeferredResult<TaskResult<RecordFetchResult<Object, Object>>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<RecordFetchResult<Object, Object>> result =
                        kafkaManager.getTopicRecordFetcherTaskExecutor()
                        .executeDetailed(request.getTopicName(), request.getFetchRequest());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

    @PostMapping("/topic-purger")
    public DeferredResult<TaskResult<Void>> topicPurger(
            @RequestBody TopicPurgeRequest request) {
        DeferredResult<TaskResult<Void>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<Void> result = kafkaManager.getTopicPurgerTaskExecutor().executeDetailed(request.getTopicName());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

    @PostMapping("/consumer-group-offset-resetter")
    public DeferredResult<TaskResult<Void>> consumerGroupOffsetResetter(
            @RequestBody ConsumerGroupOffsetResetRequest request) {
        DeferredResult<TaskResult<Void>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<Void> result = kafkaManager.getConsumerGroupOffsetResetterTaskExecutor()
                        .executeDetailed(request.getGroupName(), request.getOffsets());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

    @PostMapping("/consumer-group-topic-offset-fetcher")
    public DeferredResult<TaskResult<Map<TopicPartition, OffsetRange>>> consumerGroupTopicOffsetFetcher(
            @RequestBody ConsumerGroupTopicOffsetFetchRequest request) {
        DeferredResult<TaskResult<Map<TopicPartition, OffsetRange>>> response =
                new DeferredResult<>(properties.getAsyncRequestTimeoutInMs());
        ForkJoinPool.commonPool().submit(DelegatingSecurityContextRunnable.create(() -> {
            try {
                TaskResult<Map<TopicPartition, OffsetRange>> result =
                        kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor()
                        .executeDetailed(request.getGroupName());
                if (result.isSuccessful()) {
                    response.setResult(result);
                } else {
                    response.setErrorResult(result.getError());
                }
            } catch (Exception ex) {
                response.setErrorResult(ex);
            }
        }, null));
        return response;
    }

}
