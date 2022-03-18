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
package com.epam.eco.kafkamanager;

import java.util.Optional;
import java.util.concurrent.Future;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import com.epam.eco.kafkamanager.exec.AsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
@PreAuthorize("@authorizer.isPermitted('TOPIC', #resourceKey, 'READ')")
public interface TopicRecordCounterTaskExecutor extends AsyncStatefullTaskExecutor<String, Long> {

    @Override
    Long execute(@P("resourceKey") String topicName);

    @Override
    TaskResult<Long> executeDetailed(@P("resourceKey") String topicName);

    @Override
    Future<Long> submit(@P("resourceKey") String topicName);

    @Override
    Future<TaskResult<Long>> submitDetailed(@P("resourceKey") String topicName);

    @Override
    boolean isRunning(@P("resourceKey") String topicName);

    @Override
    Optional<TaskResult<Long>> getResult(@P("resourceKey") String topicName);

    @Override
    TaskResult<Long> getResultIfActualOrRefresh(@P("resourceKey") String topicName);

}
