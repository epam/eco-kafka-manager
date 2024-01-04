package com.epam.eco.kafkamanager.ui.config.producer;

import com.epam.eco.kafkamanager.KafkaKmProducer;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaKmUiProducer<K,V> extends KafkaKmProducer<K,V> {

    private final KafkaProducerType kafkaProducerType;
    public KafkaKmUiProducer(TopicRecordFetchParams.DataFormat keyFormat,
                             TopicRecordFetchParams.DataFormat valueFormat,
                             KafkaTemplate<K, V> kafkaTemplate) {
        super(kafkaTemplate);
        kafkaProducerType = new KafkaProducerType(keyFormat, valueFormat);
    }

    public KafkaProducerType getKafkaProducerType() {
        return kafkaProducerType;
    }

    public record KafkaProducerType (
            TopicRecordFetchParams.DataFormat keyFormat,
            TopicRecordFetchParams.DataFormat valueFormat) {}
}
