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
package com.epam.eco.kafkamanager.core.consumer.repo.zk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.core.utils.InitWaitingTreeCacheStarter;
import com.epam.eco.kafkamanager.core.utils.ZKPathUtils;

import kafka.zk.ConsumerPathZNode;

/**
 * @author Andrei_Tytsik
 */
class ZkConsumerGroupCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkConsumerGroupCache.class);

    private static final String CONSUMERS_PATH = ConsumerPathZNode.path();

    private static final String CONSUMER_REGEX = CONSUMERS_PATH + "/[^/]+";
    private static final Pattern CONSUMER_PATTERN = Pattern.compile("^" + CONSUMER_REGEX + "$");

    private static final String OWNER_REGEX = CONSUMER_REGEX + "/owners/[^/]+";
    private static final Pattern OWNER_PATTERN = Pattern.compile("^" + OWNER_REGEX + "$");

    private static final String ID_REGEX = CONSUMER_REGEX + "/ids/[^/]+";
    private static final Pattern ID_PATTERN = Pattern.compile("^" + ID_REGEX + "$");

    private static final String OFFSET_REGEX = CONSUMER_REGEX + "/offsets/[^/]+/[\\d]+";
    private static final Pattern OFFSET_PATTERN = Pattern.compile("^" + OFFSET_REGEX + "$");

    private static final int CONSUMER_GROUP_INDEX = 1;
    private static final int OFFSET_TOPIC_INDEX = 3;
    private static final int OFFSET_PARTITION_INDEX = 4;
    private static final int OWNER_TOPIC_INDEX = 3;
    private static final int ID_INDEX = 3;

    private final TreeCache consumerTreeCache;

    private final Map<String, ConsumerGroup> groupCache = new HashMap<>();
    private final Map<String, Set<String>> groupsByTopicCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public ZkConsumerGroupCache(CuratorFramework curatorFramework, CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        consumerTreeCache = TreeCache.newBuilder(curatorFramework, CONSUMERS_PATH).
                setCacheData(true).
                build();

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        consumerTreeCache.getListenable().addListener(
                (client, event) -> handleTreeEvent(event));
        InitWaitingTreeCacheStarter.with(consumerTreeCache, CONSUMERS_PATH).start();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        consumerTreeCache.close();

        LOGGER.info("Closed");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return groupCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> listGroupNames() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(groupCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String groupName) {
        lock.readLock().lock();
        try {
            return groupCache.containsKey(groupName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ConsumerGroup getGroup(String groupName) {
        lock.readLock().lock();
        try {
            ConsumerGroup group = groupCache.get(groupName);
            return group != null ? group.copyOf() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<String> getGroupsByTopicAsSet(String topicName) {
        lock.readLock().lock();
        try {
            Set<String> groupNames = groupsByTopicCache.get(topicName);
            return groupNames != null ? new HashSet<>(groupNames) : Collections.emptySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> getGroupsByTopicAsList(String topicName) {
        lock.readLock().lock();
        try {
            Set<String> groupNames = groupsByTopicCache.get(topicName);
            return groupNames != null ? new ArrayList<>(groupNames) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void handleTreeEvent(TreeCacheEvent event) {
        if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
            LOGGER.warn("ZK connection state changed: {}", event.getType());
            return;
        }

        ConsumerGroup updatedGroup = null;
        String nameOfRemovedGroup = null;

        boolean added = event.getType() == Type.NODE_ADDED;
        boolean updated = event.getType() == Type.NODE_UPDATED;
        boolean removed = event.getType() == Type.NODE_REMOVED;
        if (added || updated || removed) {
            if (isConsumerPath(event.getData().getPath())) {
                if (added || updated) {
                    updatedGroup = handleGroupUpdated(event.getData());
                } else if (removed) {
                    nameOfRemovedGroup = handleGroupRemoved(event.getData());
                }
            } else if (isOwnerPath(event.getData().getPath())) {
                if (added || updated) {
                    updatedGroup = handleTopicUpdated(event.getData());
                } else if (removed) {
                    updatedGroup = handleTopicRemoved(event.getData());
                }
            } else if (isOffsetPath(event.getData().getPath())) {
                if (added || updated) {
                    updatedGroup = handleOffsetUpdated(event.getData());
                } else if (removed) {
                    updatedGroup = handleOffsetRemoved(event.getData());
                }
            } else if (isIdPath(event.getData().getPath())) {
                if (added || updated) {
                    updatedGroup = handleMemberUpdated(event.getData());
                } else if (removed) {
                    updatedGroup = handleMemberRemoved(event.getData());
                }
            }
        }

        fireCacheListener(updatedGroup, nameOfRemovedGroup);
    }

    private ConsumerGroup handleGroupUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            return getGroupFromGroupCacheOrCreate(groupName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String handleGroupRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = groupCache.remove(groupName);

            if (group != null) {
                removeGroupFromGroupsByTopicCache(group.topicNames, groupName);
            }

            return groupName;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleTopicUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            String topicName = getTopicNameFromOwnerPath(childData.getPath());
            group.topicNames.add(topicName);

            addGroupToGroupsByTopicCache(topicName, groupName);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleTopicRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            String topicName = getTopicNameFromOwnerPath(childData.getPath());
            group.topicNames.remove(topicName);

            removeTopicFromGroupsByTopicCache(topicName);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleMemberUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            String memberId = getMemberIdFromIdPath(childData.getPath());
            group.members.add(memberId);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleMemberRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            String memberId = getMemberIdFromIdPath(childData.getPath());
            group.members.remove(memberId);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleOffsetUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            TopicPartition topicPartition = getTopicPartitionFromOffsetPath(childData.getPath());
            Long offset = toOffsetValue(childData);
            group.offsets.put(topicPartition, offset);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ConsumerGroup handleOffsetRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String groupName = getGroupNameFromConsumerPath(childData.getPath());
            ConsumerGroup group = getGroupFromGroupCacheOrCreate(groupName);

            TopicPartition topicPartition = getTopicPartitionFromOffsetPath(childData.getPath());
            group.offsets.remove(topicPartition);

            return group;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addGroupToGroupsByTopicCache(String topicName, String groupName) {
        getGroupsFromGroupsByTopicCache(topicName, true).add(groupName);
    }

    private void removeGroupFromGroupsByTopicCache(Collection<String> topicNames, String groupName) {
        topicNames.forEach(topicName -> removeGroupFromGroupsByTopicCache(topicName, groupName));
    }

    private void removeGroupFromGroupsByTopicCache(String topicName, String groupName) {
        Set<String> groupNames = getGroupsFromGroupsByTopicCache(topicName, false);
        if (groupNames != null) {
            groupNames.remove(groupName);
            if (groupNames.isEmpty()) {
                removeTopicFromGroupsByTopicCache(topicName);
            }
        }
    }

    private void removeTopicFromGroupsByTopicCache(String topicName) {
        groupsByTopicCache.remove(topicName);
    }

    private Set<String> getGroupsFromGroupsByTopicCache(String topicName, boolean createIfMissing) {
        Set<String> groupNames = groupsByTopicCache.get(topicName);
        if (groupNames == null && createIfMissing) {
            groupNames = new TreeSet<>();
            groupsByTopicCache.put(topicName, groupNames);
        }
        return groupNames;
    }

    private Long toOffsetValue(ChildData childData) {
        String offsetString = new String(childData.getData(), StandardCharsets.UTF_8);
        return Long.valueOf(offsetString);
    }

    private ConsumerGroup getGroupFromGroupCacheOrCreate(String groupName) {
        ConsumerGroup group = groupCache.get(groupName);
        if (group == null) {
            group = new ConsumerGroup(groupName);
            groupCache.put(groupName, group);
        }
        return group;
    }

    private boolean isConsumerPath(String path) {
        return CONSUMER_PATTERN.matcher(path).matches();
    }

    private boolean isIdPath(String path) {
        return ID_PATTERN.matcher(path).matches();
    }

    private boolean isOwnerPath(String path) {
        return OWNER_PATTERN.matcher(path).matches();
    }

    private boolean isOffsetPath(String path) {
        return OFFSET_PATTERN.matcher(path).matches();
    }

    private String getGroupNameFromConsumerPath(String path) {
        return ZKPathUtils.getPathToken(path, CONSUMER_GROUP_INDEX);
    }

    private TopicPartition getTopicPartitionFromOffsetPath(String path) {
        String topic = getTopicFromOffsetPath(path);
        Integer partition = getPartitionFromOffsetPath(path);
        return new TopicPartition(topic, partition);
    }

    private String getTopicFromOffsetPath(String path) {
        return ZKPathUtils.getPathToken(path, OFFSET_TOPIC_INDEX);
    }

    private Integer getPartitionFromOffsetPath(String path) {
        return Integer.valueOf(ZKPathUtils.getPathToken(path, OFFSET_PARTITION_INDEX));
    }

    private String getTopicNameFromOwnerPath(String path) {
        return ZKPathUtils.getPathToken(path, OWNER_TOPIC_INDEX);
    }

    private String getMemberIdFromIdPath(String path) {
        return ZKPathUtils.getPathToken(path, ID_INDEX);
    }

    private void fireCacheListener(ConsumerGroup updatedGroup, String nameOfRemovedGroup) {
        if (updatedGroup != null) {
            try {
                cacheListener.onGroupUpdated(updatedGroup);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'group updated' event. Group = %s",
                                updatedGroup),
                        ex);
            }
        }
        if (nameOfRemovedGroup != null) {
            try {
                cacheListener.onGroupRemoved(nameOfRemovedGroup);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'group removed' event. Group name = %s",
                                nameOfRemovedGroup),
                        ex);
            }
        }
    }

    public class ConsumerGroup {

        public final String name;
        public final Set<String> topicNames = new HashSet<>();
        public final Set<String> members = new HashSet<>();
        public final Map<TopicPartition, Long> offsets = new HashMap<>();

        public ConsumerGroup(String name) {
            Validate.notBlank(name, "Name is blank");

            this.name = name;
        }

        public ConsumerGroup copyOf() {
            ConsumerGroup copy = new ConsumerGroup(this.name);
            copy.topicNames.addAll(this.topicNames);
            copy.offsets.putAll(this.offsets);
            copy.members.addAll(this.members);
            return copy;
        }

    }

    public static interface CacheListener {
        void onGroupUpdated(ConsumerGroup group);
        void onGroupRemoved(String groupName);
    }

}
