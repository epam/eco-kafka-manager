package com.epam.eco.kafkamanager.core.broker.repo.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.Node;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataKey;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class KafkaBrokerRepoTest {

    @Mock
    private KafkaAdminOperations admin;
    @Mock
    private MetadataRepo metadataRepo;
    @InjectMocks
    private KafkaBrokerRepo repo;


    @Test
    void returnsBrokerInfoWhenBrokerExists() {
        // Given
        int brokerId = 1;
        Config config = new Config(Set.of(
                new ConfigEntry("listener.security.protocol.map", "PLAINTEXT:PLAINTEXT," +
                        "OAUTH:SASL_SSL,OAUTHBEARER:SASL_SSL"),
                new ConfigEntry("advertised.listeners", "PLAINTEXT://localhost:9092," +
                        "OAUTH://localhost:9096,OAUTHBEARER://localhost:9097"),
                new ConfigEntry("inter.broker.protocol.version", "3.6")
        ));

        Node node = new Node(brokerId, "localhost", 9092, "rack1");
        when(admin.describeBrokerConfig(eq(brokerId))).thenReturn(config);
        when(admin.describeCluster()).thenReturn(List.of(node));
        when(metadataRepo.get(eq(BrokerMetadataKey.with(brokerId))))
                .thenReturn(Metadata.builder().description("test").build());

        // When
        BrokerInfo info = repo.get(brokerId);

        // Then
        assertNotNull(info);
        assertEquals(brokerId, info.getId());
        assertEquals(3, info.getEndPoints().size());
        assertEquals("rack1", info.getRack());
        assertTrue(info.getConfig().containsKey("inter.broker.protocol.version"));
    }

    @Test
    void throwsNotFoundExceptionWhenBrokerDoesNotExist() {
        // Given
        int brokerId = 999;
        when(admin.describeBrokerConfig(eq(brokerId))).thenReturn(null);

        // When/Then
        assertThrows(NotFoundException.class, () -> repo.get(brokerId));
    }

    @Test
    void valuesReturnsAllBrokers() {
        // Given
        Node node1 = new Node(1, "localhost", 9092, "rack1");
        Node node2 = new Node(2, "localhost", 9093, "rack2");
        Config config = getConfig();

        when(admin.describeCluster()).thenReturn(List.of(node1, node2));
        when(admin.describeBrokerConfigs(anyList()))
                .thenReturn(Map.of(1, config, 2, config));
        when(metadataRepo.get(any())).thenReturn(Metadata.builder().build());

        // When
        List<BrokerInfo> brokers = repo.values();

        // Then
        assertEquals(2, brokers.size());
        assertTrue(brokers.stream().anyMatch(b -> b.getId() == 1));
        assertTrue(brokers.stream().anyMatch(b -> b.getId() == 2));
    }

    @Test
    void valuesWithSpecificBrokerIdsReturnsRequestedBrokers() {
        // Given
        Node node1 = new Node(1, "localhost", 9092, "rack1");
        Node node2 = new Node(2, "localhost", 9093, "rack2");
        Config config = getConfig();

        when(admin.describeCluster()).thenReturn(List.of(node1, node2));
        when(admin.describeBrokerConfigs(eq(List.of(1))))
                .thenReturn(Map.of(1, config));
        when(metadataRepo.get(eq(BrokerMetadataKey.with(1))))
                .thenReturn(Metadata.builder().build());

        // When
        List<BrokerInfo> brokers = repo.values(List.of(1));

        // Then
        assertEquals(1, brokers.size());
        assertEquals(1, brokers.get(0).getId());
    }

    @NotNull
    private static Config getConfig() {
        return new Config(Set.of(
                new ConfigEntry("listener.security.protocol.map", "PLAINTEXT:PLAINTEXT"),
                new ConfigEntry("inter.broker.protocol.version", "3.6"),
                new ConfigEntry("advertised.listeners", "PLAINTEXT://localhost:9092")
        ));
    }

    @Test
    void updateConfigUpdatesAndReturnsUpdatedBrokerInfo() {
        // Given
        int brokerId = 1;
        Config newConfig = getConfig();
        Node node = new Node(brokerId, "localhost", 9092, "rack1");

        when(admin.describeBrokerConfig(eq(brokerId))).thenReturn(newConfig);
        when(admin.describeCluster()).thenReturn(List.of(node));
        when(metadataRepo.get(eq(BrokerMetadataKey.with(brokerId))))
                .thenReturn(Metadata.builder().build());

        // When
        BrokerInfo updatedInfo = repo.updateConfig(
                brokerId,
                Map.of("inter.broker.protocol.version", "3.6")
        );

        // Then
        assertNotNull(updatedInfo);
        verify(admin).alterBrokerConfigs(eq(brokerId), anyMap());
    }

    @Test
    void updateConfigThrowsExceptionWhenBrokerDoesNotExist() {
        // Given
        int brokerId = 999;
        when(admin.describeCluster()).thenReturn(List.of(new Node(1, "localhost", 9092, "rack1")));

        // When/Then
        assertThrows(NotFoundException.class, () ->
                repo.updateConfig(brokerId, Map.of("some.config", "value"))
        );
        verify(admin, never()).alterBrokerConfigs(anyInt(), anyMap());
    }

    @Test
    void sizeReturnsNumberOfBrokers() {
        // Given
        when(admin.describeCluster()).thenReturn(List.of(
                new Node(1, "localhost", 9092, "rack1"),
                new Node(2, "localhost", 9093, "rack2")
        ));

        // When
        int size = repo.size();

        // Then
        assertEquals(2, size);
    }

    @Test
    void containsReturnsTrueWhenBrokerExists() {
        // Given
        when(admin.describeCluster()).thenReturn(List.of(
                new Node(1, "localhost", 9092, "rack1"),
                new Node(2, "localhost", 9093, "rack2")
        ));

        // When/Then
        assertTrue(repo.contains(1));
        assertFalse(repo.contains(999));
    }
}
