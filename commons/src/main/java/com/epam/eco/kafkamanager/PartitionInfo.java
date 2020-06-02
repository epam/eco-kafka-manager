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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class PartitionInfo {

    private final TopicPartition id;
    private final List<Integer> replicas;
    private final Integer leader;
    private final List<Integer> isr;
    private final List<Integer> underReplicated;

    public PartitionInfo(
            @JsonProperty("topic") String topic,
            @JsonProperty("partition") int partition,
            @JsonProperty("replicas") List<Integer> replicas,
            @JsonProperty("leader") Integer leader,
            @JsonProperty("isr") List<Integer> isr) {
        this(
                topic != null && partition >= 0 ? new TopicPartition(topic, partition) : null,
                replicas,
                leader,
                isr);
    }

    public PartitionInfo(
            TopicPartition id,
            List<Integer> replicas,
            Integer leader,
            List<Integer> isr) {
        Validate.notNull(id, "Id is null");
        Validate.notNull(replicas, "Replica list is null");
        Validate.noNullElements(replicas, "Replica list contains null elements");
        if (isr != null) {
            Validate.noNullElements(isr, "ISR list can't contains null elements");
        }

        this.id = id;
        this.replicas = Collections.unmodifiableList(new ArrayList<>(replicas));
        this.leader = leader;
        this.isr =
                isr != null ?
                Collections.unmodifiableList(new ArrayList<>(isr)) :
                Collections.emptyList();

        underReplicated = calculateUnderReplicated();
    }

    public String getTopic() {
        return id.topic();
    }
    public int getPartition() {
        return id.partition();
    }
    @JsonIgnore
    public TopicPartition getId() {
        return id;
    }
    public List<Integer> getReplicas() {
        return replicas;
    }
    public Integer getLeader() {
        return leader;
    }
    public List<Integer> getIsr() {
        return isr;
    }
    @JsonIgnore
    public boolean isUnderReplicated() {
        return !underReplicated.isEmpty();
    }

    private List<Integer> calculateUnderReplicated() {
        return Collections.unmodifiableList(
                (List<Integer>)CollectionUtils.disjunction(replicas, isr));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, replicas, leader, isr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PartitionInfo that = (PartitionInfo)obj;
        return
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.replicas, that.replicas) &&
                Objects.equals(this.leader, that.leader) &&
                Objects.equals(this.isr, that.isr);
    }

    @Override
    public String toString() {
        return
                "{id: " + id +
                ", replicas: " + replicas +
                ", leader: " + leader +
                ", isr: " + isr +
                "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TopicPartition id;
        private List<Integer> replicas = new ArrayList<>();
        private Integer leader;
        private List<Integer> isr = new ArrayList<>();

        public Builder id(TopicPartition id) {
            this.id = id;
            return this;
        }
        public Builder replicas(List<Integer> replicas) {
            this.replicas.clear();
            if (replicas != null) {
                this.replicas.addAll(replicas);
            }
            return this;
        }
        public Builder leader(Integer leader) {
            this.leader = leader;
            return this;
        }
        public Builder isr(List<Integer> isr) {
            this.isr.clear();
            if (isr != null) {
                this.isr.addAll(isr);
            }
            return this;
        }

        public PartitionInfo build() {
            return new PartitionInfo(
                    id,
                    replicas,
                    leader,
                    isr);
        }

    }

}
