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
package com.epam.eco.kafkamanager.exec;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Andrei_Tytsik
 */
public class AbstractAsyncStatefullTaskExecutorTest {

    @Test
    public void testSimlpeAsyncTaskIsExecuted() throws Exception {
        try (SynchronizedParrotTaskExecutor executor = new SynchronizedParrotTaskExecutor("Doubloons")) {
            Future<TaskResult<String>> future = executor.submitDetailed("");
            Assertions.assertNotNull(future);

            executor.awaitStarted();

            Assertions.assertTrue(executor.isRunning(""));

            executor.signalCanContinue();

            TaskResult<String> result = future.get();
            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getStartedAt());
            Assertions.assertNotNull(result.getFinishedAt());
            Assertions.assertNotNull(result.getElapsedFormattedAsHMS());
            Assertions.assertNull(result.getError());
            Assertions.assertEquals("Doubloons", result.getValue());
        }
    }

    @Test
    public void testSimlpeSyncTaskIsExecuted() throws Exception {
        try (SynchronizedParrotTaskExecutor executor = new SynchronizedParrotTaskExecutor("Jolly Roger")) {
            Future<TaskResult<String>> future =
                    Executors.newSingleThreadExecutor().submit(() -> executor.executeDetailed(""));
            Assertions.assertNotNull(future);

            executor.awaitStarted();

            Assertions.assertTrue(executor.isRunning(""));

            executor.signalCanContinue();

            TaskResult<String> result = future.get();
            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getStartedAt());
            Assertions.assertNotNull(result.getFinishedAt());
            Assertions.assertNotNull(result.getElapsedFormattedAsHMS());
            Assertions.assertNull(result.getError());
            Assertions.assertEquals("Jolly Roger", result.getValue());
        }
    }

    @Test
    public void testPendingFutureIsResolvedForAsyncRun() throws Exception {
        try (SynchronizedParrotTaskExecutor executor = new SynchronizedParrotTaskExecutor("Old Salt")) {
            Future<TaskResult<String>> future1 = executor.submitDetailed("");
            Assertions.assertNotNull(future1);

            Future<TaskResult<String>> future2 = executor.submitDetailed("");
            Assertions.assertNotNull(future2);
            Assertions.assertEquals(future1, future2);

            executor.awaitStarted();

            Assertions.assertTrue(executor.isRunning(""));

            executor.signalCanContinue();

            Assertions.assertEquals("Old Salt", future1.get().getValue());
            Assertions.assertEquals("Old Salt", future2.get().getValue());
        }
    }

    @Test
    public void testPendingFuntureIsResolvedForSyncRun() throws Exception {
        try (SynchronizedParrotTaskExecutor executor = new SynchronizedParrotTaskExecutor("Seadog")) {
            Future<TaskResult<String>> future =
                    Executors.newSingleThreadExecutor().submit(() -> executor.executeDetailed(""));

            executor.awaitStarted();

            Assertions.assertTrue(executor.isRunning(""));

            Future<TaskResult<String>> future1 = executor.submitDetailed("");
            Assertions.assertNotNull(future1);

            Future<TaskResult<String>> future2 = executor.submitDetailed("");
            Assertions.assertNotNull(future2);
            Assertions.assertEquals(future1, future2);

            executor.signalCanContinue();

            Assertions.assertEquals("Seadog", future.get().getValue());
            Assertions.assertEquals("Seadog", future1.get().getValue());
            Assertions.assertEquals("Seadog", future2.get().getValue());
        }
    }

    @Test
    public void testLastFreshResultIsResolvedPriorToRunningNewTask() throws Exception {
        try (SynchronizedParrotTaskExecutor executor = new SynchronizedParrotTaskExecutor("Bottle of Rum")) {
            Future<TaskResult<String>> future = executor.submitDetailed("");
            Assertions.assertNotNull(future);

            executor.awaitStarted();

            Thread.sleep(500);

            executor.signalCanContinue();

            TaskResult<String> result1 = future.get();
            TaskResult<String> result2 = executor.getResultIfActualOrRefresh("");

            Assertions.assertEquals(result1, result2);
        }
    }

    private class SimpleParrotTaskExecutor extends AbstractAsyncStatefullTaskExecutor<String, String> {

        private final Collection<String> phrases;
        private Iterator<String> iterator;

        public SimpleParrotTaskExecutor(String ... phrases) {
            this(Arrays.asList(phrases));
        }

        public SimpleParrotTaskExecutor(Collection<String> phrases) {
            super(Executors.newSingleThreadExecutor(), testCacheManager());
            this.phrases = phrases;
            this.iterator = phrases.iterator();
        }

        @Override
        protected TaskResult<String> doExecute(String resourceKey) {
            return TaskResult.of(() -> {
                return sayNextPhrase();
            });
        }

        protected String sayNextPhrase() {
            if (!iterator.hasNext()) {
                iterator = phrases.iterator();
            }
            return iterator.next();
        }

    }

    private class SynchronizedParrotTaskExecutor extends SimpleParrotTaskExecutor {

        private final CountDownLatch latchStarted = new CountDownLatch(1);
        private final CountDownLatch latchCanContinue = new CountDownLatch(1);

        public SynchronizedParrotTaskExecutor(String phrase) {
            super(phrase);
        }

        @Override
        protected TaskResult<String> doExecute(String resourceKey) {
            return TaskResult.of(() -> {
                signalStarted();
                awaitCanContinue();
                return sayNextPhrase();
            });
        }

        private void awaitStarted() {
            try {
                latchStarted.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }

        private void awaitCanContinue() {
            try {
                latchCanContinue.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }

        private void signalCanContinue() {
            latchCanContinue.countDown();
        }

        private void signalStarted() {
            latchStarted.countDown();
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static CacheManager testCacheManager() {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = new MapBackedCache();
        Mockito.when(
                cacheManager.createCache(Mockito.anyString(), Mockito.any(Configuration.class))).
                thenReturn(cache);
        return cacheManager;
    }

    static class MapBackedCache<K, V> implements Cache<K, V> {

        private final Map<K, V> map = new HashMap<>();

        @Override
        public V get(K key) {
            return map.get(key);
        }

        @Override
        public Map<K, V> getAll(Set<? extends K> keys) {
            return null;
        }

        @Override
        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        @Override
        public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        }

        @Override
        public void put(K key, V value) {
            map.put(key, value);
        }

        @Override
        public V getAndPut(K key, V value) {
            return map.get(key);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            map.putAll((Map) map);
        }

        @Override
        public boolean putIfAbsent(K key, V value) {
            return map.putIfAbsent(key, value) != null;
        }

        @Override
        public boolean remove(K key) {
            return map.remove(key) != null;
        }

        @Override
        public boolean remove(K key, V oldValue) {
            return map.remove(key, oldValue);
        }

        @Override
        public V getAndRemove(K key) {
            return null;
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return map.replace(key, oldValue, newValue);
        }

        @Override
        public boolean replace(K key, V value) {
            return map.replace(key, value) != null;
        }

        @Override
        public V getAndReplace(K key, V value) {
            return map.get(key);
        }

        @Override
        public void removeAll(Set<? extends K> keys) {
        }

        @Override
        public void removeAll() {
            map.clear();
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
            return null;
        }

        @Override
        public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
            return null;
        }

        @Override
        public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public CacheManager getCacheManager() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            return null;
        }

        @Override
        public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        }

        @Override
        public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return null;
        }

    }

}
