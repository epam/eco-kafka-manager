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

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.Validate;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;

/**
 * @author Andrei_Tytsik
 */
public abstract class AbstractAsyncTaskExecutor<R, I, V> extends AbstractTaskExecutor<R, I, V> implements AsyncTaskExecutor<R, I, V>, Closeable {

    private final ExecutorService executor;

    public AbstractAsyncTaskExecutor() {
        this(Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * 2));
    }

    public AbstractAsyncTaskExecutor(ExecutorService executor) {
        Validate.notNull(executor, "Executor is null");

        this.executor = executor;
    }

    @Override
    public Future<V> submit(R resourceKey, I input) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return executor.submit(
                DelegatingSecurityContextCallable.create(
                        () -> execute(resourceKey, input), null));
    }

    @Override
    public Future<TaskResult<V>> submitDetailed(R resourceKey, I input) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        return executor.submit(
                DelegatingSecurityContextCallable.create(
                        () -> executeDetailed(resourceKey, input), null));
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

}
