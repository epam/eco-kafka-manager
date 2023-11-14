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
package com.epam.eco.kafkamanager.core.topic.repo.zk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.utils.ZKPaths;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.core.utils.InitWaitingTreeCacheStarter;
import com.epam.eco.kafkamanager.core.utils.ZKPathUtils;
import com.epam.eco.kafkamanager.utils.MapperUtils;

import kafka.zk.TopicsZNode;

/**
 * @author Andrei_Tytsik
 */
class ZkTopicCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkTopicCache.class);

    private static final String TOPICS_PATH = TopicsZNode.path();

    private static final String TOPIC_REGEX = TOPICS_PATH + "/[^/]+";
    private static final Pattern TOPIC_PATTERN = Pattern.compile("^" + TOPIC_REGEX + "$");

    private static final String STATE_REGEX = TOPIC_REGEX + "/partitions/[\\d]+/state";
    private static final Pattern STATE_PATTERN = Pattern.compile("^" + STATE_REGEX + "$");

    private static final int STATE_PARTITION_INDEX = 4;
    private static final int STATE_TOPIC_INDEX = 2;

    private static final String VERSION = "version";

    private static final String TOPIC_PARTITIONS = "partitions";

    private static final String STATE_LEADER = "leader";
    private static final String STATE_ISR = "isr";

    private final TreeCache topicTreeCache;

    private final Map<String, Topic> topicCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public ZkTopicCache(
            CuratorFramework curatorFramework,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        topicTreeCache = TreeCache.newBuilder(curatorFramework, TOPICS_PATH).
                setCacheData(true).
                build();

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        topicTreeCache.getListenable().addListener(
                (client, event) -> handleTreeEvent(event));
        InitWaitingTreeCacheStarter.with(topicTreeCache, TOPICS_PATH).start();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        topicTreeCache.close();

        LOGGER.info("Closed");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return topicCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String topicName) {
        lock.readLock().lock();
        try {
            return topicCache.containsKey(topicName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> listTopicNames() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(topicCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Topic getTopic(String topicName) {
        lock.readLock().lock();
        try {
            Topic topic = topicCache.get(topicName);
            return topic != null ? topic.copyOf() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T, E extends Throwable> T callIfTopicAbsentOrElseThrow(
            String topicName,
            Callable<T> callable,
            Supplier<E> exception) throws E {
        Validate.notNull(callable, "Callable can't be null");
        Validate.notNull(exception, "Exception can't be null");

        lock.readLock().lock();
        try {
            if (!topicCache.containsKey(topicName)) {
                try {
                    return callable.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw exception.get();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T, E extends Throwable> T callIfTopicPresentOrElseThrow(
            String topicName,
            Function<Topic, T> function,
            Supplier<E> exception) throws E {
        Validate.notNull(function, "Function can't be null");
        Validate.notNull(exception, "Exception can't be null");

        lock.readLock().lock();
        try {
            Topic topic = topicCache.get(topicName);
            if (topic != null) {
                return function.apply(topic.copyOf());
            } else {
                throw exception.get();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void handleTreeEvent(TreeCacheEvent event) {
        if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
            LOGGER.warn("ZK connection state changed: {}", event.getType());
            return;
        }

        Topic updatedTopic = null;
        String nameOfRemovedTopic = null;

        boolean added = event.getType() == Type.NODE_ADDED;
        boolean updated = event.getType() == Type.NODE_UPDATED;
        boolean removed = event.getType() == Type.NODE_REMOVED;
        if (added || updated || removed) {
            if (isTopicPath(event.getData().getPath())) {
                if (added || updated) {
                    updatedTopic = handleTopicUpdated(event.getData());
                } else if (removed) {
                    nameOfRemovedTopic = handleTopicRemoved(event.getData());
                }
            } else if (isStatePath(event.getData().getPath())) {
                if (added || updated) {
                    updatedTopic = handlePartitionStateUpdated(event.getData());
                } else if (removed) {
                    updatedTopic = handlePartitionStateRemoved(event.getData());
                }
            }
        }

        fireCacheListener(updatedTopic, nameOfRemovedTopic);
    }

    private Topic handleTopicUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromTopicPath(childData.getPath());
            Topic topic = getTopicFromCacheOrCreate(topicName);

            Map<TopicPartition, PartitionMetadata> partitions = toPartitions(childData);
            topic.partitions.putAll(partitions);

            return topic;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String handleTopicRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromTopicPath(childData.getPath());
            topicCache.remove(topicName);
            return topicName;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Topic handlePartitionStateUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromStatePath(childData.getPath());
            Topic topic = getTopicFromCacheOrCreate(topicName);

            Integer partition = getPartitionFromStatePath(childData.getPath());
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            PartitionState state = toState(childData);
            topic.states.put(topicPartition, state);

            return topic;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Topic handlePartitionStateRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromStatePath(childData.getPath());
            Topic topic = getTopicFromCacheOrCreate(topicName);

            Integer partition = getPartitionFromStatePath(childData.getPath());
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            topic.states.remove(topicPartition);

            return topic;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Topic getTopicFromCacheOrCreate(String topicName) {
        Topic topic = topicCache.get(topicName);
        if (topic == null) {
            topic = new Topic(topicName);
            topicCache.put(topicName, topic);
        }
        return topic;
    }

    private String getTopicNameFromTopicPath(String path) {
        return ZKPaths.getNodeFromPath(path);
    }

    private String getTopicNameFromStatePath(String path) {
        return ZKPathUtils.getPathToken(path, STATE_TOPIC_INDEX);
    }

    private Integer getPartitionFromStatePath(String path) {
        return Integer.valueOf(ZKPathUtils.getPathToken(path, STATE_PARTITION_INDEX));
    }

    private boolean isTopicPath(String path) {
        return TOPIC_PATTERN.matcher(path).matches();
    }

    private boolean isStatePath(String path) {
        return STATE_PATTERN.matcher(path).matches();
    }

    private Map<TopicPartition, PartitionMetadata> toPartitions(ChildData childData) {
        String topicInfoString = new String(childData.getData(), StandardCharsets.UTF_8);
        Map<String, Object> topicInfoMap = MapperUtils.jsonToMap(topicInfoString);

        String topicName = getTopicNameFromTopicPath(childData.getPath());

        Integer version = (Integer)topicInfoMap.get(VERSION);
        if (version >= 1 && version <= 3) {
            return toPartitions_V1_V2_V3(topicName, topicInfoMap);
        } else {
            throw new RuntimeException(
                    String.format("Unsupported topic version: %s", topicInfoString));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<TopicPartition, PartitionMetadata> toPartitions_V1_V2_V3(
            String topicName,
            Map<String, Object> topicInfoMap) {
        Map<String, Object> partitionsInfoMap = (Map<String, Object>)topicInfoMap.get(TOPIC_PARTITIONS);

        Map<TopicPartition, PartitionMetadata> partitions = new HashMap<>();
        partitionsInfoMap.forEach((key, value) -> {
            int partitionId = Integer.parseInt(key);
            List<Integer> replicas = (List<Integer>) value;
            partitions.put(
                    new TopicPartition(topicName, partitionId),
                    new PartitionMetadata(replicas));
        });

        return partitions;
    }

    private PartitionState toState(ChildData childData) {
        String stateInfoString = new String(childData.getData(), StandardCharsets.UTF_8);
        Map<String, Object> stateInfoMap = MapperUtils.jsonToMap(stateInfoString);

        Integer version = (Integer)stateInfoMap.get(VERSION);
        if (version == 1) {
            return toStateV1(stateInfoMap);
        } else {
            throw new RuntimeException(
                    String.format("Unsupported state version: %s", stateInfoString));
        }
    }

    @SuppressWarnings("unchecked")
    private PartitionState toStateV1(Map<String, Object> stateInfoMap) {
        Integer leader = (Integer)stateInfoMap.get(STATE_LEADER);
        List<Integer> isr = (List<Integer>)stateInfoMap.get(STATE_ISR);
        return new PartitionState(leader, isr);
    }

    private void fireCacheListener(Topic updatedTopic, String nameOfRemovedTopic) {
        if (updatedTopic != null) {
            try {
                cacheListener.onTopicUpdated(updatedTopic);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'topic updated' event. Topic = %s",
                                updatedTopic),
                        ex);
            }
        }
        if (nameOfRemovedTopic != null) {
            try {
                cacheListener.onTopicRemoved(nameOfRemovedTopic);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'topic removed' event. Topic name = %s",
                                nameOfRemovedTopic),
                        ex);
            }
        }
    }

    public class Topic {

        public final String name;
        public final Map<TopicPartition, PartitionMetadata> partitions = new HashMap<>();
        public final Map<TopicPartition, PartitionState> states = new HashMap<>();

        public Topic(String name) {
            Validate.notBlank(name, "Name is blank");

            this.name = name;
        }

        public Topic copyOf() {
            Topic copy = new Topic(this.name);
            copy.partitions.putAll(this.partitions);
            copy.states.putAll(this.states);
            return copy;
        }

    }

    public static class PartitionMetadata {

        public final List<Integer> replicas;

        public PartitionMetadata(List<Integer> replicas) {
            Validate.notNull(replicas, "Replica list is null");
            Validate.noNullElements(replicas, "Replica list contains null elements");

            this.replicas = replicas;
        }

    }

    public static class PartitionState {

        public final Integer leader;
        public final List<Integer> isr;

        public PartitionState(Integer leader, List<Integer> isr) {
            this.leader = leader;
            this.isr = isr != null ? isr : Collections.emptyList();
        }

    }

    public interface CacheListener {
        void onTopicUpdated(Topic topic);
        void onTopicRemoved(String topicName);
    }

}
