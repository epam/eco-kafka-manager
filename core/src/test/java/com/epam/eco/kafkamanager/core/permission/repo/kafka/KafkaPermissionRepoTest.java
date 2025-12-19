package com.epam.eco.kafkamanager.core.permission.repo.kafka;

import java.util.Collections;
import java.util.List;

import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataKey;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class KafkaPermissionRepoTest {

    @Mock
    private KafkaAdminOperations admin;
    @Mock
    private MetadataRepo metadataRepo;

    @InjectMocks
    private KafkaPermissionRepo repo;

    private final KafkaPrincipal testPrincipal = new KafkaPrincipal("User", "test-user");
    private final String testTopic = "test-topic";

    @BeforeEach
    void setUp() {
        when(metadataRepo.get(any(PermissionMetadataKey.class)))
                .thenReturn(Metadata.builder().description("test").build());
    }

    @Test
    void valuesReturnsAllPermissions() {
        // Given
        AclBinding b1 = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        testPrincipal.toString(),
                        "*",
                        AclOperation.READ,
                        AclPermissionType.ALLOW)
        );
        AclBinding b2 = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        testPrincipal.toString(),
                        "*",
                        AclOperation.WRITE,
                        AclPermissionType.ALLOW)
        );

        when(admin.describeAcl(eq(AclBindingFilter.ANY))).thenReturn(List.of(b1, b2));

        // When
        List<PermissionInfo> permissions = repo.values();

        // Then
        assertEquals(2, permissions.size());
        assertTrue(permissions.stream()
                .allMatch(p -> p.getResourceName().equals(testTopic) &&
                        p.getKafkaPrincipal().equals(testPrincipal)));
        verify(admin).describeAcl(AclBindingFilter.ANY);
    }

    @Test
    void findMatchingOfResourceReturnsMatchingPermissions() {
        // Given
        String principal = testPrincipal.toString();
        AclBinding readBinding = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        principal,
                        "*",
                        AclOperation.READ,
                        AclPermissionType.ALLOW)
        );
        AclBinding writeBinding = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        principal,
                        "*",
                        AclOperation.WRITE,
                        AclPermissionType.ALLOW)
        );

        when(admin.describeAcl(any(AclBindingFilter.class)))
                .thenReturn(List.of(readBinding, writeBinding));

        // When
        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(testTopic)
                .patternType(PatternType.LITERAL)
                .permissionTypeFilter(AclPermissionType.ALLOW)
                .operationFilter(AclOperation.READ)
                .build();

        List<PermissionInfo> result = repo.findMatchingOfResource(filter);

        // Then
        assertEquals(1, result.size());
        PermissionInfo permission = result.get(0);
        assertEquals(testTopic, permission.getResourceName());
        assertEquals(AclOperation.READ, permission.getOperation());
        assertEquals(testPrincipal, permission.getKafkaPrincipal());

        ArgumentCaptor<AclBindingFilter> filterCaptor =
                ArgumentCaptor.forClass(AclBindingFilter.class);
        verify(admin).describeAcl(filterCaptor.capture());

        AclBindingFilter usedFilter = filterCaptor.getValue();
        assertEquals(ResourceType.TOPIC, usedFilter.patternFilter().resourceType());
        assertEquals(testTopic, usedFilter.patternFilter().name());
    }

    @Test
    void createAddsNewPermission() {
        // Given
        AclBinding expectedBinding = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        testPrincipal.toString(),
                        "*",
                        AclOperation.READ,
                        AclPermissionType.ALLOW)
        );
        when(admin.describeAcl(argThat(filter ->
                filter.patternFilter().name().equals(testTopic) &&
                        filter.patternFilter().resourceType() == ResourceType.TOPIC
        ))).thenReturn(Collections.emptyList()).thenReturn(List.of(expectedBinding));


        // When
        repo.create(
                ResourceType.TOPIC,
                testTopic,
                PatternType.LITERAL,
                testPrincipal,
                AclPermissionType.ALLOW,
                AclOperation.READ,
                "*"
        );

        // Then
        ArgumentCaptor<AclBinding> bindingCaptor = ArgumentCaptor.forClass(AclBinding.class);
        verify(admin).createAcl(bindingCaptor.capture());

        AclBinding createdBinding = bindingCaptor.getValue();
        assertEquals(expectedBinding.pattern(), createdBinding.pattern());

        verify(admin, times(2)).describeAcl(argThat(filter ->
                filter.patternFilter().name().equals(testTopic) &&
                        filter.patternFilter().resourceType() == ResourceType.TOPIC
        ));
        assertEquals(expectedBinding.entry(), createdBinding.entry());
    }

    @Test
    void deleteOfResourceRemovesMatchingPermissions() {
        // Given
        AclBinding binding = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        testPrincipal.toString(),
                        "*",
                        AclOperation.READ,
                        AclPermissionType.ALLOW)
        );

        when(admin.describeAcl(any(AclBindingFilter.class)))
                .thenReturn(List.of(binding)).thenReturn(Collections.emptyList());

        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(testTopic)
                .patternType(PatternType.LITERAL)
                .permissionTypeFilter(AclPermissionType.ALLOW)
                .operationFilter(AclOperation.READ)
                .build();

        KafkaPermissionRepo.DeleteCallback callback =
                mock(KafkaPermissionRepo.DeleteCallback.class);

        // When
        repo.deleteOfResource(filter, callback);

        // Then
        ArgumentCaptor<List<AclBindingFilter>> filtersCaptor = ArgumentCaptor.forClass(List.class);
        verify(admin).deleteAcls(filtersCaptor.capture());
        verify(admin, times(2)).describeAcl(any(AclBindingFilter.class));

        List<AclBindingFilter> deletedFilters = filtersCaptor.getValue();
        assertEquals(1, deletedFilters.size());

        AclBindingFilter deletedFilter = deletedFilters.get(0);
        assertEquals(ResourceType.TOPIC, deletedFilter.patternFilter().resourceType());
        assertEquals(testTopic, deletedFilter.patternFilter().name());

        verify(callback).onBeforeDelete(argThat(permissions ->
                permissions.size() == 1 &&
                        permissions.get(0).getResourceName().equals(testTopic)
        ));
    }

    @Test
    void deleteOfResourceWithoutChecksRemovesMatchingPermissions() {
        // Given
        AclBinding binding = new AclBinding(
                new ResourcePattern(ResourceType.TOPIC, testTopic, PatternType.LITERAL),
                new org.apache.kafka.common.acl.AccessControlEntry(
                        testPrincipal.toString(),
                        "*",
                        AclOperation.READ,
                        AclPermissionType.ALLOW)
        );

        when(admin.describeAcl(any(AclBindingFilter.class)))
                .thenReturn(List.of(binding)).thenReturn(Collections.emptyList());;

        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(testTopic)
                .patternType(PatternType.LITERAL)
                .permissionTypeFilter(AclPermissionType.ALLOW)
                .operationFilter(AclOperation.READ)
                .build();

        KafkaPermissionRepo.DeleteCallback callback =
                mock(KafkaPermissionRepo.DeleteCallback.class);

        // When
        repo.deleteOfResourceWithoutChecks(filter, callback);

        // Then
        verify(admin).deleteAcls(anyList());
        verify(admin, times(2)).describeAcl(any(AclBindingFilter.class));
        verify(callback).onBeforeDelete(anyList());
    }

}
