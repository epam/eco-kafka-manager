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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrei_Tytsik
 */
public abstract class AbstractTaskExecutor<R, I, V> implements TaskExecutor<R, I, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskExecutor.class);

    @Override
    public TaskResult<V> executeDetailed(R resourceKey, I input) {
        Validate.notNull(resourceKey, "Resource key can't be null");

        TaskResult<V> result = doExecute(resourceKey, input);
        if (result.getError() != null) {
            LOGGER.error("Execution of task failed", result.getError());
        }

        return result;
    }

    @Override
    public V execute(R resourceKey, I input) {
        return executeDetailed(resourceKey, input).getValue();
    }

    protected abstract TaskResult<V> doExecute(R resourceKey, I input);
}
