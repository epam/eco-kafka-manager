/*
 */
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.FilterClausePredicate;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.FetchMode;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;
import com.epam.eco.kafkamanager.ui.config.producer.KafkaManagerByteArrayProducer;

import static com.epam.eco.kafkamanager.ui.utils.HeaderUtils.getReplacedHeaderMap;

/**
 * @author Mikhail_Vershkov
 */
public class KafkaRecordRepublisherImpl implements KafkaRecordRepublisher {

    private static final int REPUBLISH_COUNT_LIMIT = 1;
    private static final long DUMMY_TIMESTAMP = 0L;
    private static final boolean IS_USE_CACHE = Boolean.FALSE;
    private static final long CACHE_EXPIRATION_TIME = 0L;
    private static final FilterClausePredicate<byte[],byte[]> DUMMY_PREDICATE = null;

    private final KafkaManagerUiProperties properties;
    private final KafkaManager kafkaManager;
    private final KafkaManagerByteArrayProducer byteArrayProducer;

    private final static BiFunction<ConsumerRecord<byte[],byte[]>, ReplacementType,byte[]> TOMBSTONE_VALUE_RESOLVER =
            (record, replacementType) -> replacementType == ReplacementType.TOMBSTONE ? null : record.value();

    public static final BiPredicate<ConsumerRecord<?,?>,String> IS_OFFSET_EQUALS =
            (record, recordId) -> (record.partition()+"_"+record.offset()).equals(recordId);

    public KafkaRecordRepublisherImpl(
            KafkaManagerUiProperties properties,
            KafkaManager kafkaManager,
            KafkaManagerByteArrayProducer producer) {
         this.properties = properties;
         this.kafkaManager = kafkaManager;
         this.byteArrayProducer = producer;
    }

    @Override
    public @NotNull ResponseEntity<String> republish(
            String topicName,
            ReplacementType replacementType,
            String recordId,
            String headers,
            Integer timeout
    ) {
        try {
            ConsumerRecord<byte[], byte[]> record = getOriginalRecord(topicName, recordId, timeout);
            return ResponseEntity.ok(byteArrayProducer.send(topicName,
                                                            record.key(),
                                                            TOMBSTONE_VALUE_RESOLVER.apply(record,replacementType),
                                                            getReplacedHeaderMap(replacementType, headers,
                                                                                 properties.getTopicBrowser())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    private ConsumerRecord<byte[], byte[]> getOriginalRecord(
            String topicName,
            String recordId,
            Integer timeout
    ) {
        List<ConsumerRecord<byte[], byte[]>> records = getFetchedRecords(topicName, recordId, timeout);
        return records.stream()
                .filter(r->IS_OFFSET_EQUALS.test(r, recordId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find record with partition_offset=" + recordId));
    }

    private List<ConsumerRecord<byte[], byte[]>> getFetchedRecords(
            String topicName,
            String recordId,
            Integer timeout
    ) {
        TaskResult<RecordFetchResult<byte[], byte[]>> result =
                getRecordFetchedResults(topicName, resolveRepublishParams(recordId, timeout));
        return result.getValue().getRecords();
    }

    private TaskResult<RecordFetchResult<byte[], byte[]>> getRecordFetchedResults(
            String topicName,
            TopicRecordFetchParams<byte[], byte[]> fetchParams
    ) {
        TopicRecordFetcherTaskExecutor<byte[], byte[]> executor = kafkaManager.getTopicRecordFetcherTaskExecutor();
        return executor.executeDetailed(topicName, fetchParams);
    }

    private static Map<Integer, OffsetRange> parseOffsets(String recordId) {
        if(isDividerNotExists(recordId)) {
            throw new IllegalArgumentException("Record id parse exception: There is no divider '_' between partition and offset");
        }
        String[] tokens = recordId.split("_");
        long offset = Long.parseLong(tokens[1]);
        int partition = Integer.parseInt(tokens[0]);
        return Map.of(partition, new OffsetRange(offset,Boolean.TRUE,offset,Boolean.TRUE));
    }

    private static boolean isDividerNotExists(String recordId) {
        return !recordId.contains("_");
    }


    public static @NotNull TopicRecordFetchParams<byte[], byte[]> resolveRepublishParams(
            String recordId,
            Integer timeout
    ) {
        return new TopicRecordFetchParams<>(
                TopicRecordFetchParams.DataFormat.BYTE_ARRAY,
                TopicRecordFetchParams.DataFormat.BYTE_ARRAY,
                parseOffsets(recordId),
                REPUBLISH_COUNT_LIMIT,
                timeout,
                FetchMode.FETCH_RANGE,
                DUMMY_TIMESTAMP,
                IS_USE_CACHE,
                CACHE_EXPIRATION_TIME,
                DUMMY_PREDICATE
        );
    }

}
