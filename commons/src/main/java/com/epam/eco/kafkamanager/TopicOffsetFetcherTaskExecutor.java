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
package com.epam.eco.kafkamanager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.exec.AsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
public interface TopicOffsetFetcherTaskExecutor extends AsyncStatefullTaskExecutor<String, Map<TopicPartition, OffsetRange>> {

    @Override
    Map<TopicPartition, OffsetRange> execute(String topicName);

    @Override
    TaskResult<Map<TopicPartition, OffsetRange>> executeDetailed(String topicName);

    @Override
    Future<Map<TopicPartition, OffsetRange>> submit(String topicName);

    @Override
    Future<TaskResult<Map<TopicPartition, OffsetRange>>> submitDetailed(String topicName);

    @Override
    boolean isRunning(String topicName);

    @Override
    Optional<TaskResult<Map<TopicPartition, OffsetRange>>> getResult(String topicName);

    @Override
    TaskResult<Map<TopicPartition, OffsetRange>> getResultIfActualOrRefresh(String topicName);

    Map<TopicPartition, OffsetTimeSeries> getOffsetTimeSeries(String topicName);

}
