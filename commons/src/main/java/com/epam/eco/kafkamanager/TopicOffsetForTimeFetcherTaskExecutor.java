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

import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.springframework.security.core.parameters.P;

import com.epam.eco.kafkamanager.exec.TaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Naira_Tamrazyan
 */
public interface TopicOffsetForTimeFetcherTaskExecutor extends TaskExecutor<String, Long, Map<TopicPartition, Long>> {

    @Override
    Map<TopicPartition, Long> execute(@P("resourceKey") String topicName, Long timestamp);

    @Override
    TaskResult<Map<TopicPartition, Long>> executeDetailed(@P("resourceKey") String topicName, Long timestamp);

}
