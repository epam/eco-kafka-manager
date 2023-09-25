package com.epam.eco.kafkamanager.utils;

import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;

public class KafkaUtils {
    public static int getPartitionByKey(String key, int partitionsCount) {
        return Utils.toPositive(Utils.murmur2(key.getBytes(StandardCharsets.UTF_8))) % partitionsCount;
    }
}
