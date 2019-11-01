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
package com.epam.eco.kafkamanager;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import com.epam.eco.kafkamanager.exec.TaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
@PreAuthorize("@authorizer.isPermitted('TOPIC', #resourceKey, 'WRITE')")
public interface TopicPurgerTaskExecutor extends TaskExecutor<String, Void, Void> {

    default Void execute(@P("resourceKey") String topicName) {
        return execute(topicName, null);
    }

    default TaskResult<Void> executeDetailed(@P("resourceKey") String topicName) {
        return executeDetailed(topicName, null);
    }

    @Override
    Void execute(@P("resourceKey") String topicName, Void input);

    @Override
    TaskResult<Void> executeDetailed(@P("resourceKey") String topicName, Void input);

}