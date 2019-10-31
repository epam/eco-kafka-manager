/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.core.permission.repo.zk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceFilter;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.concurrent.ResourceSemaphores;
import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataKey;
import com.epam.eco.kafkamanager.PermissionRepo;
import com.epam.eco.kafkamanager.PermissionSearchQuery;
import com.epam.eco.kafkamanager.core.permission.repo.zk.ZkAclCache.ACL;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import kafka.security.auth.Acl;
import kafka.security.auth.Resource;

/**
 * @author Andrei_Tytsik
 */
public class ZkPermissionRepo extends AbstractValueRepo<PermissionInfo, PermissionSearchQuery> implements PermissionRepo, CachedRepo<Resource>, ZkAclCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZkPermissionRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private CuratorFramework curatorFramework;
    @Autowired
    private MetadataRepo metadataRepo;

    private ZkAclCache aclCache;

    private final Map<Resource, Set<PermissionInfo>> permissionInfoCache = new ConcurrentHashMap<>();

    private final ResourceSemaphores<Resource, PermissionOperation> semaphores = new ResourceSemaphores<>();

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
        for (kafka.security.auth.Resource resource : aclCache.listResources()) {
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
    public void create(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host) {
        Validate.notNull(resourceType, "Resource type is null");
        Validate.notBlank(resourceName, "Resource name is blank");
        Validate.notNull(principal, "Principal is null");
        Validate.notNull(permissionType, "Permission type is null");
        Validate.notNull(operation, "Operation is null");
        Validate.notBlank(host, "Host is blank");

        org.apache.kafka.common.resource.Resource resource =
                new org.apache.kafka.common.resource.Resource(
                        resourceType,
                        resourceName);
        AccessControlEntry ace = new AccessControlEntry(
                principal.toString(),
                host,
                operation,
                permissionType);
        Resource semaphoreKey = ScalaConversions.asScalaResource(resourceType, resourceName);

        ResourceSemaphores.ResourceSemaphore<Resource, PermissionOperation> semaphore = null;
        try {
            semaphore = aclCache.callInLock(() -> {
                ResourceSemaphores.ResourceSemaphore<Resource, PermissionOperation> updateSemaphore =
                        semaphores.createSemaphore(
                                semaphoreKey,
                                PermissionOperation.UPDATE);

                adminOperations.createAcl(
                        new AclBinding(
                                new ResourcePattern(resource.resourceType(), resource.name(), PatternType.LITERAL),
                                ace));

                return updateSemaphore;
            });

            semaphore.awaitUnchecked();
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void delete(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host) {
        Validate.notNull(resourceType, "Resource type is null");
        Validate.notBlank(resourceName, "Resource name is blank");
        Validate.notNull(principal, "Principal is null");
        Validate.notNull(permissionType, "Permission type is null");
        Validate.notNull(operation, "Operation is null");
        Validate.notBlank(host, "Host is blank");

        ResourceFilter resourceFilter = new ResourceFilter(
                resourceType,
                resourceName);
        AccessControlEntryFilter aceFilter = new AccessControlEntryFilter(
                principal.toString(),
                host,
                operation,
                permissionType);
        AclBindingFilter aclBindingFilter = new AclBindingFilter(
                new ResourcePatternFilter(
                        resourceFilter.resourceType(),
                        resourceFilter.name(),
                        PatternType.LITERAL),
                aceFilter);
        Resource semaphoreKey = ScalaConversions.asScalaResource(resourceType, resourceName);

        ResourceSemaphores.ResourceSemaphore<Resource, PermissionOperation> semaphore = null;
        try {
            semaphore = aclCache.callInLock(() -> {
                ResourceSemaphores.ResourceSemaphore<Resource, PermissionOperation> updateSemaphore =
                        semaphores.createSemaphore(
                                semaphoreKey,
                                PermissionOperation.DELETE);

                adminOperations.deleteAcl(aclBindingFilter);

                return updateSemaphore;
            });

            semaphore.awaitUnchecked();
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void evict(Resource resource) {
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
    public void onAclRemoved(Resource resource) {
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
        Resource resource = ScalaConversions.asScalaResource(
                permissionKey.getResourceType(),
                permissionKey.getResourceName());
        removePermissionsFromInfoCache(resource);
    }

    @Override
    public void onMetadataRemoved(MetadataKey key) {
        Validate.notNull(key, "Metadata key is null");

        if (key.getEntityType() != EntityType.PERMISSION) {
            return;
        }

        PermissionMetadataKey permissionKey = (PermissionMetadataKey)key;
        Resource resource = ScalaConversions.asScalaResource(
                permissionKey.getResourceType(),
                permissionKey.getResourceName());
        removePermissionsFromInfoCache(resource);
    }

    private void removePermissionsFromInfoCache(Resource resource) {
        permissionInfoCache.remove(resource);
    }

    private Set<PermissionInfo> getPermissionsFromInfoCacheOrCreate(
            Resource resource) {
        return permissionInfoCache.computeIfAbsent(
                resource,
                key -> {
                    ACL acl = aclCache.getAcl(resource);
                    return acl != null ? toInfos(acl) : null;
                });
    }

    private Set<PermissionInfo> toInfos(ACL acl) {
        return acl.permissions.stream().
                map((permission) -> toInfo(acl.resource, permission)).
                collect(Collectors.toSet());
    }

    private PermissionInfo toInfo(Resource resource, Acl permission) {
        return PermissionInfo.builder().
                kafkaPrincipal(permission.principal()).
                resourceType(resource.resourceType().toJava()).
                resourceName(resource.name()).
                permissionType(permission.permissionType().toJava()).
                operation(permission.operation().toJava()).
                host(permission.host()).
                metadata(
                        metadataRepo.get(
                                PermissionMetadataKey.with(
                                        permission.principal(),
                                        resource.resourceType().toJava(),
                                        resource.name()))).
                build();
    }

}
