package com.epam.eco.kafkamanager;

import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;

public class PartitionByKeyResolverImpl implements PartitionByKeyResolver {
    public int getPartitionByKey(String key, int partitionsCount) {
        return Utils.toPositive(Utils.murmur2(key.getBytes(StandardCharsets.UTF_8))) % partitionsCount;
    }
}
