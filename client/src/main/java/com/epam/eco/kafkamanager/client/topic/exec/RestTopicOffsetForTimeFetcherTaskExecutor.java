package com.epam.eco.kafkamanager.client.topic.exec;

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

import com.epam.eco.kafkamanager.TopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.rest.request.TopicOffsetForTimeFetchRequest;

public class RestTopicOffsetForTimeFetcherTaskExecutor extends
        AbstractTaskExecutor<String, Long, Map<TopicPartition, Long>> implements TopicOffsetForTimeFetcherTaskExecutor {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;


    @Override
    public TaskResult<Map<TopicPartition, Long>> doExecute(String topicName, Long timestamp) {
        Date start = new Date();
        try {
            ResponseEntity<TaskResult<Map<TopicPartition, Long>>> response = restTemplate.exchange(
                    "/api/tasks/topic-offset-time-fetcher",
                    HttpMethod.POST,
                    new HttpEntity<>(new TopicOffsetForTimeFetchRequest(topicName, timestamp)),
                    new ParameterizedTypeReference<TaskResult<Map<TopicPartition, Long>>>() {
                    });
            return response.getBody();
        } catch (RestClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return TaskResult.<Map<TopicPartition, Long>>builder()
                    .error(ex)
                    .startedAt(start)
                    .finishedAtNow()
                    .build();
        }
    }
}
