/*
 * Copyright 2020 EPAM Systems
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
package com.epam.eco.kafkamanager.exec;

import java.io.Closeable;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;

/**
 * @author Andrei_Tytsik
 */
public abstract class AbstractAsyncStatefullTaskExecutor<R, V> implements AsyncStatefullTaskExecutor<R, V>, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncStatefullTaskExecutor.class);

    private static final String CACHE_NAME = "task_workers_";

    private final ExecutorService executor;
    private final Cache<Object, Object> workerCache;

    public AbstractAsyncStatefullTaskExecutor(CacheManager cacheManager) {
        this(
                Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * 2),
                cacheManager);
    }

    public AbstractAsyncStatefullTaskExecutor(
            ExecutorService executor,
            CacheManager cacheManager) {
        Validate.notNull(executor, "Executor is null");
        Validate.notNull(cacheManager, "Cache manager is null");

        this.executor = executor;

        workerCache = cacheManager.createCache(
                CACHE_NAME + getClass().getSimpleName(),
                new MutableConfiguration<>().
                    setTypes(Object.class, Object.class).
                    setStoreByValue(false).
                    setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(Duration.ONE_DAY)));
    }

    @Override
    public V execute(R resourceKey) {
        Validate.notNull(resourceKey,"Resource key can't be null");

        return executeDetailed(resourceKey).getValue();
    }

    @Override
    public TaskResult<V> executeDetailed(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return getWorker(resourceKey, true).execute();
    }

    @Override
    public Future<V> submit(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return new ValueRetrievingFutureWrapper<>(submitDetailed(resourceKey));
    }

    @Override
    public Future<TaskResult<V>> submitDetailed(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return getWorker(resourceKey, true).submit();
    }

    @Override
    public boolean isRunning(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        Worker worker = getWorker(resourceKey, false);
        return worker != null && worker.isRunning();
    }

    @Override
    public Optional<TaskResult<V>> getResult(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        Worker worker = getWorker(resourceKey, false);
        return Optional.ofNullable(worker != null ? worker.getResult() : null);
    }

    @Override
    public TaskResult<V> getResultIfActualOrRefresh(R resourceKey) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return getWorker(resourceKey, true).getResultIfActualOrRefresh();
    }

    protected abstract TaskResult<V> doExecute(R resourceKey);

    @SuppressWarnings("unchecked")
    private Worker getWorker(R resourceKey, boolean createIfAbsent) {
        Worker worker = (Worker)workerCache.get(resourceKey);
        if (worker == null && createIfAbsent) {
            synchronized (workerCache) {
                worker = (Worker)workerCache.get(resourceKey);
                if (worker == null) {
                    worker = new Worker(resourceKey);
                    workerCache.put(resourceKey, worker);
                }
            }
        }
        return worker;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private class Worker {

        final R resourceKey;
        final AtomicReference<FutureTask<TaskResult<V>>> futureRef = new AtomicReference<>();
        final AtomicReference<TaskResult<V>> resultRef = new AtomicReference<>();

        Worker(R resourceKey) {
            this.resourceKey = resourceKey;
        }

        boolean isRunning() {
            FutureTask<TaskResult<V>> future = futureRef.get();
            return future != null && !future.isDone();
        }

        TaskResult<V> getResult() {
            return resultRef.get();
        }

        TaskResult<V> getResultIfActualOrRefresh() {
            TaskResult<V> result = resultRef.get();
            if (result != null && result.isSuccessful() && isActual(result)) {
                return result;
            } else {
                return execute();
            }
        }

        TaskResult<V> execute() {
            FutureTask<TaskResult<V>> future = createTaskOrReferPending(false);
            future.run();
            try {
                return future.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            } catch (ExecutionException ee) {
                throw (RuntimeException)ee.getCause();
            }
        }

        FutureTask<TaskResult<V>> submit() {
            return createTaskOrReferPending(true);
        }

        synchronized FutureTask<TaskResult<V>> createTaskOrReferPending(boolean submit) {
            FutureTask<TaskResult<V>> future = futureRef.get();
            if (future == null) {
                future = new FutureTask<>(DelegatingSecurityContextCallable.create(
                        () -> {
                            TaskResult<V> result = doExecute(resourceKey);
                            if (result.getError() != null) {
                                LOGGER.error("Execution of task failed", result.getError());
                            }

                            completeTask(result);
                            return result;
                        },
                        null));
                futureRef.set(future);
                if (submit) {
                    executor.submit(() -> futureRef.get().run());
                }
            }
            return future;
        }

        synchronized void completeTask(TaskResult<V> result) {
            resultRef.set(result);
            futureRef.set(null);
        }

    }

    private static class ValueRetrievingFutureWrapper<V> implements Future<V> {

        private final Future<TaskResult<V>> future;

        ValueRetrievingFutureWrapper(Future<TaskResult<V>> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return future.get().getValue();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit).getValue();
        }

    }

    private static final double DEFAULT_ACTUALITY_RATIO = 20.d;

    private static boolean isActual(TaskResult<?> result) {
        long elapsed = result.getElapsed();
        long durationSinceRun = new Date().getTime() - result.getFinishedAt().getTime();
        return elapsed * DEFAULT_ACTUALITY_RATIO > durationSinceRun;
    }

}
