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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.utils.ZKPaths;
import org.apache.kafka.common.resource.PatternType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.core.utils.InitWaitingTreeCacheStarter;
import com.epam.eco.kafkamanager.core.utils.ZKPathUtils;

import kafka.security.auth.Acl;
import kafka.security.auth.Resource;
import kafka.zk.LiteralAclStore;

/**
 * @author Andrei_Tytsik
 */
class ZkAclCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkAclCache.class);

    private static final String ACL_PATH = LiteralAclStore.aclPath();

    private static final String RESOURCE_REGEX = ACL_PATH + "/[^/]+/[^/]+";
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^" + RESOURCE_REGEX + "$");

    private static final int RESOURCE_TYPE_INDEX = 1;

    private final TreeCache aclTreeCache;

    private final Map<Resource, ACL> aclCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public ZkAclCache(
            CuratorFramework curatorFramework,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        aclTreeCache = TreeCache.newBuilder(curatorFramework, ACL_PATH).
                setCacheData(true).
                build();

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        aclTreeCache.getListenable().addListener((client, event) -> handleEvent(event));
        InitWaitingTreeCacheStarter.with(aclTreeCache, ACL_PATH).start();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        aclTreeCache.close();

        LOGGER.info("Closed");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return aclCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int countPermissions() {
        lock.readLock().lock();
        try {
            return aclCache.values().stream().mapToInt(acl -> acl.permissions.size()).sum();
        } finally {
            lock.readLock().unlock();
        }
    }

    public ACL getAcl(Resource resource) {
        lock.readLock().lock();
        try {
            ACL acl = aclCache.get(resource);
            return acl != null ? acl.copyOf() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Resource> listResources() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(aclCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T callInLock(Callable<T> callable) {
        Validate.notNull(callable, "Callable can't be null");

        lock.readLock().lock();
        try {
            try {
                return callable.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void handleEvent(TreeCacheEvent event) {
        if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
            LOGGER.warn("ZK connection state changed: {}", event.getType());
            return;
        }

        ACL updatedAcl = null;
        Resource resourceOfRemovedAcl = null;

        boolean added = event.getType() == Type.NODE_ADDED;
        boolean updated = event.getType() == Type.NODE_UPDATED;
        boolean removed = event.getType() == Type.NODE_REMOVED;
        if ((added || updated || removed) && isResourcePath(event.getData().getPath())) {
            if (added || updated) {
                updatedAcl = handleAclUpdated(event.getData());
            } else if (removed) {
                resourceOfRemovedAcl = handleAclRemoved(event.getData());
            }
        }

        fireCacheListener(updatedAcl, resourceOfRemovedAcl);
    }

    private ACL handleAclUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String resourceType = getResourceTypeFromPath(childData.getPath());
            String resourceName = getResourceNameFromPath(childData.getPath());
            Resource resource = toResource(resourceType, resourceName);
            ACL acl = toAcl(resource, childData);
            aclCache.put(resource, acl);
            return acl;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Resource handleAclRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String resourceType = getResourceTypeFromPath(childData.getPath());
            String resourceName = getResourceNameFromPath(childData.getPath());
            Resource resource = toResource(resourceType, resourceName);
            aclCache.remove(resource);
            return resource;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ACL toAcl(Resource resource, ChildData childData) {
        Set<Acl> acls = ScalaConversions.asJavaSet(Acl.fromBytes(childData.getData()));
        return new ACL(resource, acls);
    }

    private boolean isResourcePath(String path) {
        return RESOURCE_PATTERN.matcher(path).matches();
    }

    private Resource toResource(String resourceType, String resourceName) {
        return Resource.fromString(resourceType + Resource.Separator() + resourceName);
    }

    private String getResourceTypeFromPath(String path) {
        return ZKPathUtils.getPathToken(path, RESOURCE_TYPE_INDEX);
    }

    private String getResourceNameFromPath(String path) {
        return ZKPaths.getNodeFromPath(path);
    }

    private void fireCacheListener(ACL updatedAcl, Resource resourceOfRemovedAcl) {
        if (updatedAcl != null) {
            try {
                cacheListener.onAclUpdated(updatedAcl);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'ACL updated' event. ACL = %s",
                                updatedAcl),
                        ex);
            }
        }
        if (resourceOfRemovedAcl != null) {
            try {
                cacheListener.onAclRemoved(resourceOfRemovedAcl);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'ACL removed' event. Resource = %s",
                                resourceOfRemovedAcl),
                        ex);
            }
        }
    }

    public class ACL {

        public final Resource resource;
        public final Set<Acl> permissions;

        public ACL(Resource resource, Set<Acl> permissions) {
            Validate.notNull(resource, "Resource is null");

            this.resource = resource;
            this.permissions = permissions != null ? permissions : Collections.emptySet();
        }

        public ACL copyOf() {
            return new ACL(
                    new Resource(resource.resourceType(), resource.name(), PatternType.LITERAL),
                    new HashSet<>(permissions));
        }

    }

    public static interface CacheListener {
        void onAclUpdated(ACL acl);
        void onAclRemoved(Resource resource);
    }

}
