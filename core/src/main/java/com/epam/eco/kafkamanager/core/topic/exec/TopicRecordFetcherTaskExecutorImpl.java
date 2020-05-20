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
package com.epam.eco.kafkamanager.core.topic.exec;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.config.ConsumerConfigBuilder;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.commons.kafka.helpers.TopicRecordFetcher;
import com.epam.eco.commons.kafka.serde.HexStringDeserializer;
import com.epam.eco.commons.kafka.serde.JsonStringDeserializer;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.RecordFetchRequest;
import com.epam.eco.kafkamanager.RecordFetchRequest.DataFormat;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

/**
 * @author Andrei_Tytsik
 */
public class TopicRecordFetcherTaskExecutorImpl<K, V> extends AbstractTaskExecutor<String, RecordFetchRequest, RecordFetchResult<K, V>> implements TopicRecordFetcherTaskExecutor<K, V> {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    @Override
    protected TaskResult<RecordFetchResult<K, V>> doExecute(String resourceKey, RecordFetchRequest input) {
        return TaskResult.of(() -> executeInternal(resourceKey, input));
    }

    public RecordFetchResult<K, V> executeInternal(String topicName, RecordFetchRequest request) {
        Validate.notNull(request, "Request is null");

        kafkaManager.getTopic(topicName); // sanity check just for case topic doesn't exist

        TopicRecordFetcher<K, V> recordFetcher = TopicRecordFetcher.
                with(buildConsumerConfig(request));

        //search by timestamps
        if(request.getFetchByTimestamp()) {
            return recordFetcher.fetchByTimestamps(
                    request.getPartitionTimestamps().entrySet().stream().
                            collect(Collectors.toMap(
                                    e -> new TopicPartition(topicName, e.getKey()),
                                    e -> e.getValue())),
                    request.getLimit(),
                    request.getTimeoutInMs());
        }

        //search by offsets
        return recordFetcher.fetchByOffsets(
                request.getOffsets().entrySet().stream().
                    collect(Collectors.toMap(
                            e -> new TopicPartition(topicName, e.getKey()),
                            e -> e.getValue())),
                request.getLimit(),
                request.getTimeoutInMs());
    }

    private Map<String, Object> buildConsumerConfig(RecordFetchRequest request) {
        return properties.buildCommonConsumerConfig(builder -> {
            initDeserializerConfig(builder, request.getKeyDataFormat(), true);
            initDeserializerConfig(builder, request.getValueDataFormat(), false);
        });
    }

    private void initDeserializerConfig(
            ConsumerConfigBuilder builder,
            DataFormat dataFormat,
            boolean isKey) {
        if (DataFormat.AVRO == dataFormat) {
            builder.deserializer(KafkaAvroDeserializer.class, isKey);
        } else if (DataFormat.STRING == dataFormat) {
            builder.deserializer(StringDeserializer.class, isKey);
        } else if (DataFormat.JSON_STRING == dataFormat) {
            builder.deserializer(JsonStringDeserializer.class, isKey);
        } else if (DataFormat.HEX_STRING == dataFormat) {
            builder.deserializer(HexStringDeserializer.class, isKey);
        } else {
            throw new IllegalArgumentException(
                    String.format("Data format '%s' not supported", dataFormat));
        }
    }

}
