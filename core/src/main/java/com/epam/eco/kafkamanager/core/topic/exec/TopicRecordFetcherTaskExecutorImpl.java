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
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;

/**
 * @author Andrei_Tytsik
 */
public class TopicRecordFetcherTaskExecutorImpl<K, V> extends AbstractTaskExecutor<String, TopicRecordFetchParams, RecordFetchResult<K, V>> implements TopicRecordFetcherTaskExecutor<K, V> {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    @Override
    protected TaskResult<RecordFetchResult<K, V>> doExecute(String resourceKey, TopicRecordFetchParams input) {
        return TaskResult.of(() -> executeInternal(resourceKey, input));
    }

    public RecordFetchResult<K, V> executeInternal(String topicName, TopicRecordFetchParams params) {
        Validate.notNull(params, "Params object is null");

        kafkaManager.getTopic(topicName); // sanity check just for case topic doesn't exist

        TopicRecordFetcher<K, V> recordFetcher = TopicRecordFetcher.
                with(buildConsumerConfig(params));

        return recordFetcher.fetchByOffsets(
                params.getOffsets().entrySet().stream().
                    collect(Collectors.toMap(
                            e -> new TopicPartition(topicName, e.getKey()),
                            e -> e.getValue())),
                params.getLimit(),
                params.getTimeoutInMs());
    }

    private Map<String, Object> buildConsumerConfig(TopicRecordFetchParams params) {
        return properties.buildCommonConsumerConfig(builder -> {
            initDeserializerConfig(builder, params.getKeyDataFormat(), true);
            initDeserializerConfig(builder, params.getValueDataFormat(), false);
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
        } else if (DataFormat.PROTOCOL_BUFFERS == dataFormat) {
            builder.deserializer(KafkaProtobufDeserializer.class, isKey);
        } else {
            throw new IllegalArgumentException(
                    String.format("Data format '%s' not supported", dataFormat));
        }
    }

}
