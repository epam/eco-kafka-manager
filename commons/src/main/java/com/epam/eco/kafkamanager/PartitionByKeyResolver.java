package com.epam.eco.kafkamanager;

import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;

public interface PartitionByKeyResolver {
    int getPartitionByKey(String key, int partitionsCount);
}
