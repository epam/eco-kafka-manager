package com.epam.eco.kafkamanager.core.topic.repo.kafka;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class KafkaTopicRepoTest {

    @Mock
    private KafkaAdminOperations admin;
    @Mock
    private MetadataRepo metadataRepo;
    @InjectMocks
    private KafkaTopicRepo repo;


    @Test
    void getReturnsTopicInfoWhenTopicExists() {
        // Given
        String topic = "test-topic";
        TopicDescription td = buildTopicDescription(topic, 3, 2);
        Config cfg = new Config(Set.of(new ConfigEntry("cleanup.policy", "delete")));
        Metadata md = Metadata.builder().description("test-desc").updatedBy("tester").build();

        when(admin.describeTopics(eq(Collections.singleton(topic))))
                .thenReturn(Map.of(topic, td));
        when(admin.describeTopicConfig(eq(topic))).thenReturn(cfg);
        when(metadataRepo.get(eq(TopicMetadataKey.with(topic)))).thenReturn(md);

        // When
        TopicInfo info = repo.get(topic);

        // Then
        assertNotNull(info);
        assertEquals(topic, info.getName());
        assertEquals(3, info.getPartitionCount());
        assertEquals(2, info.getReplicationFactor());
        assertFalse(info.hasUnderReplicatedPartitions());
        assertEquals("test-desc", info.getMetadata().map(Metadata::getDescription).orElse(null));
        
        verify(admin).describeTopics(anyCollection());
        verify(admin).describeTopicConfig(eq(topic));
        verify(metadataRepo).get(eq(TopicMetadataKey.with(topic)));
    }

    @Test
    void throwsNotFoundExceptionWhenGetUnknownTopic() {
        String missing = "missing";
        when(admin.describeTopics(eq(Collections.singleton(missing))))
                .thenThrow(new UnknownTopicOrPartitionException("missing"));

        assertThrows(com.epam.eco.kafkamanager.NotFoundException.class, () -> repo.get(missing));
    }

    @Test
    void createTopicSuccessfullyAndWaitCreatedNewTopic() {
        // Given
        String topic = "new-topic";
        TopicDescription td = buildTopicDescription(topic, 1, 1);
        Config cfg = new Config(Set.of(new ConfigEntry("cleanup.policy", "delete")));
        Metadata md = Metadata.builder().description("created").updatedBy("tester").build();

        doNothing().when(admin).createTopic(eq(topic), eq(1), eq(1), anyMap());
        when(admin.describeTopics(eq(Collections.singleton(topic)))).thenReturn(Map.of(topic, td));
        when(admin.describeTopicConfig(eq(topic))).thenReturn(cfg);
        when(admin.topicExists(eq(topic))).thenReturn(false).thenReturn(false).thenReturn(true);
        when(metadataRepo.get(eq(TopicMetadataKey.with(topic)))).thenReturn(md);

        // When
        TopicInfo created = repo.create(topic, 1, 1, Map.of("cleanup.policy", "delete"));

        // Then
        assertNotNull(created);
        assertEquals(topic, created.getName());
        assertEquals(1, created.getPartitionCount());
        verify(admin,times(3)).topicExists(eq(topic));
        verify(admin).createTopic(eq(topic), eq(1), eq(1), anyMap());
    }

    @Test
    void updateConfigUpdatesTopicConfiguration() {
        // Given
        String topic = "topic-update";
        TopicDescription td = buildTopicDescription(topic, 1, 1);
        Config updatedConfig = new Config(Set.of(new ConfigEntry("retention.ms", "2000")));
        Metadata md = Metadata.builder().description("test").build();

        when(admin.topicExists(eq(topic))).thenReturn(true);
        when(admin.describeTopics(eq(Collections.singleton(topic))))
                .thenReturn(Map.of(topic, td));
        when(admin.describeTopicConfig(eq(topic))).thenReturn(updatedConfig);
        when(metadataRepo.get(eq(TopicMetadataKey.with(topic)))).thenReturn(md);
        doNothing().when(admin).alterTopicConfigs(eq(topic), anyMap());

        // When
        TopicInfo updated = repo.updateConfig(topic, Map.of("retention.ms", "2000"));

        // Then
        assertNotNull(updated);
        assertEquals("2000", updated.getConfig().get("retention.ms"));
        verify(admin).alterTopicConfigs(eq(topic), anyMap());
    }

    @Test
    void createPartitionsThrowsWhenNewCountNotGreaterThanCurrent() {
        // Given
        String topic = "topic-partitions";
        TopicDescription td = buildTopicDescription(topic, 2, 1);
        Config cfg = new Config(Set.of(new ConfigEntry("cleanup.policy", "delete")));
        Metadata md = Metadata.builder().description("test").build();

        when(admin.describeTopics(eq(Collections.singleton(topic))))
                .thenReturn(Map.of(topic, td));
        when(admin.describeTopicConfig(eq(topic))).thenReturn(cfg);
        when(metadataRepo.get(eq(TopicMetadataKey.with(topic))))
                .thenReturn(md);

        // When / Then
        assertThrows(IllegalArgumentException.class, 
            () -> repo.createPartitions(topic, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> repo.createPartitions(topic, 2));
    }
    
    @Test
    void createPartitionsSuccessfullyAddsPartitions() {
        // Given
        String topic = "topic-partitions";
        TopicDescription tdBefore = buildTopicDescription(topic, 2, 1);
        TopicDescription tdAfter = buildTopicDescription(topic, 3, 1);
        Config cfg = new Config(Set.of(new ConfigEntry("cleanup.policy", "delete")));
        Metadata md = Metadata.builder().description("test").build();

        when(admin.describeTopics(eq(Collections.singleton(topic))))
                .thenReturn(Map.of(topic, tdBefore)).thenReturn(Map.of(topic, tdAfter));
        when(admin.describeTopicConfig(eq(topic))).thenReturn(cfg);
        when(metadataRepo.get(eq(TopicMetadataKey.with(topic))))
                .thenReturn(md);
        doNothing().when(admin).createPartitions(eq(topic), eq(3));

        // When
        TopicInfo updated = repo.createPartitions(topic, 3);

        // Then
        assertNotNull(updated);
        assertEquals(3, updated.getPartitionCount());
        verify(admin).createPartitions(eq(topic), eq(3));
    }

    @Test
    void deleteTopicRemovesTopic() {
        // Given
        String topic = "topic-delete";

        when(admin.topicExists(eq(topic))).thenReturn(true);
        doNothing().when(admin).deleteTopic(eq(topic));

        // When
        repo.delete(topic);

        // Then
        verify(admin).deleteTopic(eq(topic));
    }

    private TopicDescription buildTopicDescription(
            String name,
            int partitions,
            int replicationFactor
    ) {
        List<Node> replicaNodes = IntStream.range(0, replicationFactor)
                .mapToObj(i -> new Node(i, "host" + i, 9092 + i, "rack" + i))
                .collect(Collectors.toList());
        Node leader = replicaNodes.get(0);
        List<TopicPartitionInfo> tpis = IntStream.range(0, partitions)
                .mapToObj(p -> new TopicPartitionInfo(p, leader, replicaNodes, replicaNodes))
                .collect(Collectors.toList());
        return new TopicDescription(name, false, tpis);
    }

}
