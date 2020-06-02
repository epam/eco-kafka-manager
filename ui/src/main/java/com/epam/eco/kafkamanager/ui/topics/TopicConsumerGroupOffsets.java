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
package com.epam.eco.kafkamanager.ui.topics;

import java.time.LocalDateTime;
import java.util.Objects;

import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.KafkaUtils;
import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.TopicPartitionComparator;

/**
 * @author Andrei_Tytsik
 */
public class TopicConsumerGroupOffsets implements Comparable<TopicConsumerGroupOffsets> {

    private String groupName;
    private TopicPartition partition;
    private OffsetRange topicOffsets;
    private Long topicOffsetRpm;
    private Long consumerOffset;
    private Long consumerOffsetRpm;
    private LocalDateTime consumerCommitDate;
    private boolean consumerOffsetActualForMinute;

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public TopicPartition getPartition() {
        return partition;
    }
    public void setPartition(TopicPartition partition) {
        this.partition = partition;
    }
    public OffsetRange getTopicOffsets() {
        return topicOffsets;
    }
    public void setTopicOffsets(OffsetRange topicOffsets) {
        this.topicOffsets = topicOffsets;
    }
    public Long getTopicOffsetRpm() {
        return topicOffsetRpm;
    }
    public void setTopicOffsetRpm(Long topicOffsetRpm) {
        this.topicOffsetRpm = topicOffsetRpm;
    }
    public Long getConsumerOffset() {
        return consumerOffset;
    }
    public void setConsumerOffset(Long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }
    public Long getLag() {
        if (topicOffsets == null || consumerOffset == null) {
            return null;
        }

        long lag = KafkaUtils.calculateConsumerLag(topicOffsets, consumerOffset);
        return lag >= 0 ? lag : 0;
    }
    public Long getConsumerOffsetRpm() {
        return consumerOffsetRpm;
    }
    public void setConsumerOffsetRpm(Long consumerOffsetRpm) {
        this.consumerOffsetRpm = consumerOffsetRpm;
    }
    public LocalDateTime getConsumerCommitDate() {
        return consumerCommitDate;
    }
    public void setConsumerCommitDate(LocalDateTime consumerCommitDate) {
        this.consumerCommitDate = consumerCommitDate;
    }
    public boolean isConsumerOffsetActualForMinute() {
        return consumerOffsetActualForMinute;
    }
    public void setConsumerOffsetActualForMinute(boolean consumerOffsetActualForMinute) {
        this.consumerOffsetActualForMinute = consumerOffsetActualForMinute;
    }

    @Override // is not consistent with equals
    public int compareTo(TopicConsumerGroupOffsets o) {
        int result = groupName.compareTo(o.groupName);
        if (result == 0) {
            result = TopicPartitionComparator.INSTANCE.compare(partition, o.partition);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicConsumerGroupOffsets that = (TopicConsumerGroupOffsets) o;
        return consumerOffsetActualForMinute == that.consumerOffsetActualForMinute &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(partition, that.partition) &&
                Objects.equals(topicOffsets, that.topicOffsets) &&
                Objects.equals(topicOffsetRpm, that.topicOffsetRpm) &&
                Objects.equals(consumerOffset, that.consumerOffset) &&
                Objects.equals(consumerOffsetRpm, that.consumerOffsetRpm) &&
                Objects.equals(consumerCommitDate, that.consumerCommitDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, partition, topicOffsets, topicOffsetRpm, consumerOffset,
                consumerOffsetRpm, consumerCommitDate, consumerOffsetActualForMinute);
    }
}
