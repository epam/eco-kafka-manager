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
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.utils.ZKPaths;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.core.utils.InitWaitingTreeCacheStarter;
import com.epam.eco.kafkamanager.core.utils.ZKPathUtils;

import kafka.security.authorizer.AclEntry;
import kafka.zk.ExtendedAclStore;
import kafka.zk.LiteralAclStore;

/**
 * @author Andrei_Tytsik
 */
class ZkAclCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkAclCache.class);

    private static final String LITERAL_ACL_PATH = LiteralAclStore.aclPath();
    private static final String PREFIXED_ACL_PATH = new ExtendedAclStore(PatternType.PREFIXED).aclPath();

    private static final String LITERAL_RESOURCE_REGEX = LITERAL_ACL_PATH + "/[^/]+/[^/]+";
    private static final Pattern LITERAL_RESOURCE_PATTERN = Pattern.compile("^" + LITERAL_RESOURCE_REGEX + "$");

    private static final String PREFIXED_RESOURCE_REGEX = PREFIXED_ACL_PATH + "/[^/]+/[^/]+";
    private static final Pattern PREFIXED_RESOURCE_PATTERN = Pattern.compile("^" + PREFIXED_RESOURCE_REGEX + "$");

    private static final int LITERAL_RESOURCE_TYPE_INDEX = 1;
    private static final int PREFIXED_RESOURCE_TYPE_INDEX = 2;

    private final TreeCache literalAclTreeCache;
    private final TreeCache prefixedAclTreeCache;

    private final EventHandler literalEventHandler = new EventHandler(
            PatternType.LITERAL,
            LITERAL_RESOURCE_PATTERN,
            LITERAL_RESOURCE_TYPE_INDEX);
    private final EventHandler prefixedEventHandler = new EventHandler(
            PatternType.PREFIXED,
            PREFIXED_RESOURCE_PATTERN,
            PREFIXED_RESOURCE_TYPE_INDEX);

    private final Map<ResourcePattern, ACL> aclCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public ZkAclCache(
            CuratorFramework curatorFramework,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        literalAclTreeCache = TreeCache.newBuilder(curatorFramework, LITERAL_ACL_PATH).
                setCacheData(true).
                build();

        prefixedAclTreeCache = TreeCache.newBuilder(curatorFramework, PREFIXED_ACL_PATH).
                setCacheData(true).
                build();

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        literalAclTreeCache.getListenable().addListener((client, event) -> literalEventHandler.handleEvent(event));
        InitWaitingTreeCacheStarter.with(literalAclTreeCache, LITERAL_ACL_PATH).start();

        prefixedAclTreeCache.getListenable().addListener((client, event) -> prefixedEventHandler.handleEvent(event));
        InitWaitingTreeCacheStarter.with(prefixedAclTreeCache, PREFIXED_ACL_PATH).start();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        literalAclTreeCache.close();

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

    public ACL getAcl(ResourcePattern resource) {
        lock.readLock().lock();
        try {
            ACL acl = aclCache.get(resource);
            return acl != null ? acl.copyOf() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<ResourcePattern> listResources() {
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

    private void fireCacheListener(ACL updatedAcl, ResourcePattern resourceOfRemovedAcl) {
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

    /**
     * Multi-word types (camel case) should be converted to underscore-separated string in order to
     * be correctly parsed by {@link ResourceType#fromString(String)}.
     *
     * Example: 'TransactionalId' -> 'Transactional_Id'
     *
     * @param str string to convert from
     * @return {@link ResourceType}
     */
    private static ResourceType resourceTypeFromString(String str) {
        StringBuilder builder = new StringBuilder();

        char[] chars = str.toCharArray();

        builder.append(chars[0]); // first character is ignored

        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isUpperCase(ch)) {
                builder.append('_');
            }
            builder.append(ch);
        }

        return ResourceType.fromString(builder.toString());
    }

    private class EventHandler {

        private final PatternType patternType;
        private final Pattern resourceRegex;
        private final int resourceTypeIndex;

        public EventHandler(
                PatternType patternType,
                Pattern resourceRegex,
                int resourceTypeIndex) {
            this.patternType = patternType;
            this.resourceRegex = resourceRegex;
            this.resourceTypeIndex = resourceTypeIndex;
        }

        private void handleEvent(TreeCacheEvent event) {
            if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
                LOGGER.warn("ZK connection state changed: {}", event.getType());
                return;
            }

            ACL updatedAcl = null;
            ResourcePattern resourceOfRemovedAcl = null;

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
                ResourcePattern resource = toResource(resourceType, resourceName);
                ACL acl = toAcl(resource, childData);
                aclCache.put(resource, acl);
                return acl;
            } finally {
                lock.writeLock().unlock();
            }
        }

        private ResourcePattern handleAclRemoved(ChildData childData) {
            lock.writeLock().lock();
            try {
                String resourceType = getResourceTypeFromPath(childData.getPath());
                String resourceName = getResourceNameFromPath(childData.getPath());
                ResourcePattern resource = toResource(resourceType, resourceName);
                aclCache.remove(resource);
                return resource;
            } finally {
                lock.writeLock().unlock();
            }
        }

        private ACL toAcl(ResourcePattern resource, ChildData childData) {
            Set<AclEntry> aclEntries = ScalaConversions.asJavaSet(AclEntry.fromBytes(childData.getData()));
            Set<AccessControlEntry> accessControlEntries = aclEntries.stream().
                    map(AclEntry::ace).
                    collect(Collectors.toSet());
            return new ACL(resource, accessControlEntries);
        }

        private boolean isResourcePath(String path) {
            return resourceRegex.matcher(path).matches();
        }

        private ResourcePattern toResource(String resourceType, String resourceName) {
            return new ResourcePattern(
                    resourceTypeFromString(resourceType),
                    resourceName,
                    patternType);
        }

        private String getResourceTypeFromPath(String path) {
            return ZKPathUtils.getPathToken(path, resourceTypeIndex);
        }

        private String getResourceNameFromPath(String path) {
            return ZKPaths.getNodeFromPath(path);
        }

    }

    public class ACL {

        public final ResourcePattern resource;
        public final Set<AccessControlEntry> permissions;

        public ACL(ResourcePattern resource, Set<AccessControlEntry> permissions) {
            Validate.notNull(resource, "Resource is null");

            this.resource = resource;
            this.permissions = permissions != null ? permissions : Collections.emptySet();
        }

        public ACL copyOf() {
            return new ACL(
                    resource,
                    new HashSet<>(permissions));
        }

    }

    public static interface CacheListener {
        void onAclUpdated(ACL acl);
        void onAclRemoved(ResourcePattern resource);
    }

}
