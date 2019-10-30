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
package com.epam.eco.kafkamanager.exec;

import java.util.Optional;
import java.util.concurrent.Future;

import org.springframework.security.core.parameters.P;


/**
 * @author Andrei_Tytsik
 */
public interface AsyncStatefullTaskExecutor<R, V> {
    V execute(@P("resourceKey") R resourceKey);
    TaskResult<V> executeDetailed(@P("resourceKey") R resourceKey);
    Future<V> submit(@P("resourceKey") R resourceKey);
    Future<TaskResult<V>> submitDetailed(@P("resourceKey") R resourceKey);
    boolean isRunning(@P("resourceKey") R resourceKey);
    Optional<TaskResult<V>> getResult(@P("resourceKey") R resourceKey);
    TaskResult<V> getResultIfActualOrRefresh(@P("resourceKey") R resourceKey);
}
