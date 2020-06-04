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
package com.epam.eco.kafkamanager.client.autoconfigure;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import com.epam.eco.commons.json.JsonMapper;
import com.epam.eco.commons.kafka.serde.jackson.KafkaModule;
import com.epam.eco.kafkamanager.AlreadyExistsException;
import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.TopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.client.RestKafkaManager;
import com.epam.eco.kafkamanager.client.consumer.exec.RestConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.client.consumer.exec.RestConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.client.topic.exec.RestTopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.rest.helper.SpringModule;
import com.epam.eco.kafkamanager.rest.response.MessageResponse;

/**
 * @author Raman_Babich
 */
@Configuration
@EnableConfigurationProperties({KafkaManagerClientProperties.class})
@Import({
    DisabledClientSecurityConfiguration.class})
public class KafkaManagerClientAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaManagerClientAutoConfiguration.class);

    private static final String DEFAULT_MESSAGE_RESPONSE_CONTENT =
            "Message is not available, because it doesn't appear in the response body.";

    @Autowired
    private KafkaManagerClientProperties properties;

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (int i = 0; i < messageConverters.size(); ++i) {
            if (messageConverters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                messageConverters.add(i, mappingJackson2HttpMessageConverter());
                break;
            } else if (i == messageConverters.size() - 1) {
                messageConverters.add(mappingJackson2HttpMessageConverter());
            }
        }
        restTemplate.setErrorHandler(responseErrorHandler());
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(properties.getKafkaManagerUrl()));
    }

    @Bean
    public KafkaManager kafkaManager() {
        return new RestKafkaManager();
    }

    @Bean
    public TopicRecordCounterTaskExecutor topicRecordCounterTaskExecutor() {
        return new RestTopicRecordCounterTaskExecutor(cacheManager());
    }

    @Deprecated
    @Bean
    public TopicOffsetFetcherTaskExecutor topicOffsetFetcherTaskExecutor() {
        return new RestTopicOffsetFetcherTaskExecutor(cacheManager());
    }

    @Bean
    public TopicOffsetRangeFetcherTaskExecutor topicOffsetRangeFetcherTaskExecutor() {
        return new RestTopicOffsetRangeFetcherTaskExecutor(cacheManager());
    }

    @Bean
    public TopicOffsetForTimeFetcherTaskExecutor topicOffsetForTimeRangeFetcherTaskExecutor() {
        return new RestTopicOffsetForTimeFetcherTaskExecutor();
    }

    @Bean
    public TopicPurgerTaskExecutor topicPurgerTaskExecutor() {
        return new RestTopicPurgerTaskExecutor();
    }

    @Bean
    public TopicRecordFetcherTaskExecutor<?, ?> topicRecordFetcherTaskExecutor() {
        return new RestTopicRecordFetcherTaskExecutor<>();
    }

    @Bean
    public ConsumerGroupOffsetResetterTaskExecutor consumerGroupOffsetResetterTaskExecutor() {
        return new RestConsumerGroupOffsetResetterTaskExecutor();
    }

    @Bean
    public ConsumerGroupTopicOffsetFetcherTaskExecutor consumerGroupTopicOffsetFetcherTaskExecutor() {
        return new RestConsumerGroupTopicOffsetFetcherTaskExecutor(cacheManager());
    }

    @Bean(destroyMethod="close")
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return Caching.getCachingProvider().getCacheManager();
    }

    private ObjectMapper java8KafkaSpringObjectMapper() {
        return registerSpringModule(registerJava8Module(registerKafkaModule(new ObjectMapper())));
    }

    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(java8KafkaSpringObjectMapper());
        return converter;
    }

    private ObjectMapper registerKafkaModule(ObjectMapper objectMapper) {
        return objectMapper
                .registerModule(new KafkaModule());
    }

    private ObjectMapper registerJava8Module(ObjectMapper objectMapper) {
        return objectMapper
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    private ObjectMapper registerSpringModule(ObjectMapper objectMapper) {
        return objectMapper.registerModule(new SpringModule());
    }

    private ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    MessageResponse messageResponse = toMessageResponse(response);
                    throw new NotFoundException(messageResponse.getMessage());
                } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    MessageResponse messageResponse = toMessageResponse(response);
                    throw new AlreadyExistsException(messageResponse.getMessage());
                } else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                    MessageResponse messageResponse = toMessageResponse(response);
                    throw new IllegalArgumentException(messageResponse.getMessage());
                }
                super.handleError(response);
            }
        };
    }

    private MessageResponse toMessageResponse(ClientHttpResponse response) {
        try {
            return JsonMapper.inputStreamToObject(
                    response.getBody(),
                    MessageResponse.class);
        } catch (IOException ex) {
            LOGGER.debug("Error occurs while trying to parse client response body.", ex);
        }
        return MessageResponse.with(DEFAULT_MESSAGE_RESPONSE_CONTENT);
    }

}
