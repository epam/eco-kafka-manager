package com.epam.eco.kafkamanager.ui.config.producer;

import com.epam.eco.kafkamanager.KafkaKmProducer;

import org.springframework.kafka.core.KafkaTemplate;

public class KafkaManagerByteArrayProducer extends KafkaKmProducer<byte[],byte[]> {
    public KafkaManagerByteArrayProducer(KafkaTemplate<byte[], byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }
}
