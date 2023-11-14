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
package com.epam.eco.kafkamanager.core.permission.repo.zk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.concurrent.ResourceSemaphores;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataKey;
import com.epam.eco.kafkamanager.PermissionRepo;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.core.permission.repo.zk.ZkAclCache.ACL;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

/**
 * @author Andrei_Tytsik
 */
public class ZkPermissionRepo extends AbstractValueRepo<PermissionInfo, PermissionSearchCriteria> implements PermissionRepo, CachedRepo<ResourcePattern>, ZkAclCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkPermissionRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private CuratorFramework curatorFramework;
    @Autowired
    private MetadataRepo metadataRepo;

    private ZkAclCache aclCache;

    private final Map<ResourcePattern, Set<PermissionInfo>> permissionInfoCache = new ConcurrentHashMap<>();

    private final ResourceSemaphores<ResourcePattern, PermissionOperation> semaphores = new ResourceSemaphores<>();

    @PostConstruct
    private void init() {
        initAclCache();
        subscribeOnMetadataUpdates();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startAclCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() throws Exception {
        destroyAclCache();

        LOGGER.info("Destroyed");
    }

    private void initAclCache() {
        aclCache = new ZkAclCache(curatorFramework, this);
    }

    private void startAclCache() throws Exception {
        aclCache.start();
    }

    private void destroyAclCache() throws Exception {
        aclCache.close();
    }

    private void subscribeOnMetadataUpdates() {
        metadataRepo.registerUpdateListener(this);
    }

    @Override
    public int size() {
        return aclCache.countPermissions();
    }

    @Override
    public List<PermissionInfo> values() {
        List<PermissionInfo> permissionInfos = new ArrayList<>();
        for (ResourcePattern resource : aclCache.listResources()) {
            Set<PermissionInfo> permissionInfosTmp =
                    getPermissionsFromInfoCacheOrCreate(resource);
            if (permissionInfosTmp != null) {
                permissionInfos.addAll(permissionInfosTmp);
            }
        }
        Collections.sort(permissionInfos);
        return permissionInfos;
    }

    @Override
    public List<PermissionInfo> findMatchingOfResource(ResourcePermissionFilter filter) {
        Validate.notNull(filter, "Filter is null");

        return findMatchingOfResource(filter.toAclBindingFilter());
    }

    @Override
    public void create(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host) {
        Validate.notNull(resourceType, "Resource type is null");
        Validate.notBlank(resourceName, "Resource name is blank");
        Validate.notNull(patternType, "Pattern type is null");
        Validate.notNull(principal, "Principal is null");
        Validate.notNull(permissionType, "Permission type is null");
        Validate.notNull(operation, "Operation is null");
        Validate.notBlank(host, "Host is blank");

        ResourcePattern resource = new ResourcePattern(
                resourceType,
                resourceName,
                patternType);
        AccessControlEntry entry = new AccessControlEntry(
                principal.toString(),
                host,
                operation,
                permissionType);
        AclBinding binding = new AclBinding(resource, entry);

        ResourceSemaphores.ResourceSemaphore<ResourcePattern, PermissionOperation> semaphore = null;
        try {
            semaphore = aclCache.callInLock(() -> {
                ResourceSemaphores.ResourceSemaphore<ResourcePattern, PermissionOperation> updateSemaphore =
                        semaphores.createSemaphore(
                                resource,
                                PermissionOperation.UPDATE);

                adminOperations.createAcl(binding);

                return updateSemaphore;
            });

            semaphore.awaitUnchecked();
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void deleteOfResource(
            ResourcePermissionFilter filter,
            DeleteCallback deleteCallback) {
        Validate.notNull(filter, "Filter is null");

        AclBindingFilter bindingFilter = filter.toAclBindingFilter();

        ResourcePattern resource = asResource(bindingFilter);

        ResourceSemaphores.ResourceSemaphore<ResourcePattern, PermissionOperation> semaphore = null;
        try {
            semaphore = aclCache.callInLock(() -> {
                ResourceSemaphores.ResourceSemaphore<ResourcePattern, PermissionOperation> updateSemaphore = null;

                Set<PermissionInfo> allResourcePermissions = permissionInfoCache.get(resource);
                if (!CollectionUtils.isEmpty(allResourcePermissions)) {
                    List<PermissionInfo> resourcePermissionsToDelete = findMatchingOfResource(bindingFilter);
                    if (allResourcePermissions.size() == resourcePermissionsToDelete.size()) {
                        updateSemaphore = semaphores.createSemaphore(resource, PermissionOperation.DELETE);
                    } else {
                        updateSemaphore = semaphores.createSemaphore(resource, PermissionOperation.UPDATE);
                    }

                    if (deleteCallback != null) {
                        deleteCallback.onBeforeDelete(resourcePermissionsToDelete);
                    }

                    adminOperations.deleteAcl(bindingFilter);
                }

                return updateSemaphore;
            });

            if (semaphore != null) {
                semaphore.awaitUnchecked();
            }
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void evict(ResourcePattern resource) {
        Validate.notNull(resource, "Resource in null");

        removePermissionsFromInfoCache(resource);
    }

    @Override
    public void onAclUpdated(ACL acl) {
        Validate.notNull(acl, "Acl can't be null");

        semaphores.signalDoneFor(
                acl.resource,
                PermissionOperation.UPDATE);

        removePermissionsFromInfoCache(acl.resource);
    }

    @Override
    public void onAclRemoved(ResourcePattern resource) {
        Validate.notNull(resource, "Resource can't be null");

        semaphores.signalDoneFor(
                resource,
                PermissionOperation.DELETE);

        removePermissionsFromInfoCache(resource);
    }

    @Override
    public void onMetadataUpdated(MetadataKey key, Metadata metadata) {
        Validate.notNull(key, "Metadata key is null");
        Validate.notNull(metadata, "Metadata is null");

        if (key.getEntityType() != EntityType.PERMISSION) {
            return;
        }

        PermissionMetadataKey permissionKey = (PermissionMetadataKey)key;
        ResourcePattern resource = asResource(permissionKey);
        removePermissionsFromInfoCache(resource);
    }

    @Override
    public void onMetadataRemoved(MetadataKey key) {
        Validate.notNull(key, "Metadata key is null");

        if (key.getEntityType() != EntityType.PERMISSION) {
            return;
        }

        PermissionMetadataKey permissionKey = (PermissionMetadataKey)key;
        ResourcePattern resource = asResource(permissionKey);
        removePermissionsFromInfoCache(resource);
    }

    private List<PermissionInfo> findMatchingOfResource(AclBindingFilter bindingFilter) {
        ResourcePattern resource = asResource(bindingFilter);

        Set<PermissionInfo> resourcePermissions = getPermissionsFromInfoCacheOrCreate(resource);
        if (CollectionUtils.isEmpty(resourcePermissions)) {
            return Collections.emptyList();
        }

        return resourcePermissions.stream().filter(permission ->
                (
                        StringUtils.isBlank(bindingFilter.entryFilter().principal()) ||
                        bindingFilter.entryFilter().principal().equals(permission.getKafkaPrincipal().toString())) &&
                (
                        StringUtils.isBlank(bindingFilter.entryFilter().host()) ||
                        bindingFilter.entryFilter().host().equals(permission.getHost())) &&
                (
                        bindingFilter.entryFilter().operation() == AclOperation.ANY ||
                        bindingFilter.entryFilter().operation() == permission.getOperation()) &&
                (
                        bindingFilter.entryFilter().permissionType() == AclPermissionType.ANY ||
                        bindingFilter.entryFilter().permissionType() == permission.getPermissionType())).
                collect(Collectors.toList());
    }

    private void removePermissionsFromInfoCache(ResourcePattern resource) {
        permissionInfoCache.remove(resource);
    }

    private Set<PermissionInfo> getPermissionsFromInfoCacheOrCreate(
            ResourcePattern resource) {
        return permissionInfoCache.computeIfAbsent(
                resource,
                key -> {
                    ACL acl = aclCache.getAcl(resource);
                    return acl != null ? toInfos(acl) : null;
                });
    }

    private ResourcePattern asResource(PermissionMetadataKey metadataKey) {
        return new ResourcePattern(
                metadataKey.getResourceType(),
                metadataKey.getResourceName(),
                metadataKey.getPatternType());
    }

    private ResourcePattern asResource(AclBindingFilter bindingFilter) {
        return new ResourcePattern(
                bindingFilter.patternFilter().resourceType(),
                bindingFilter.patternFilter().name(),
                bindingFilter.patternFilter().patternType());
    }


    private Set<PermissionInfo> toInfos(ACL acl) {
        return acl.permissions.stream().
                map((permission) -> toInfo(acl.resource, permission)).
                collect(Collectors.toSet());
    }

    private PermissionInfo toInfo(ResourcePattern resource, AccessControlEntry permission) {
        return PermissionInfo.builder().
                kafkaPrincipal(SecurityUtils.parseKafkaPrincipal(permission.principal())).
                resourceType(resource.resourceType()).
                resourceName(resource.name()).
                patternType(resource.patternType()).
                permissionType(permission.permissionType()).
                operation(permission.operation()).
                host(permission.host()).
                metadata(
                        metadataRepo.get(
                                PermissionMetadataKey.with(
                                        permission.principal(),
                                        resource.resourceType(),
                                        resource.name(),
                                        resource.patternType()))).
                build();
    }

}
