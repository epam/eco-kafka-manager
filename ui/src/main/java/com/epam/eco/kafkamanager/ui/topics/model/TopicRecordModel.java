/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.topics.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Mikhail_Vershkov
 */

public class TopicRecordModel implements Serializable {
    private String name;
    private Integer partitions;
    private Integer replicas;
    private Map<String,String> config;
    private List<String> consumerGroups;
    private String description;
    private String topicDataURL;
    private ExternalToolModel schemaCatalog;
    private ExternalToolModel grafanaMetrics;

    private Collection<ExternalToolModel> externalTools;

    public TopicRecordModel() {
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    @JsonGetter("partitions")
    public Integer getPartitions() {
        return partitions;
    }

    @JsonGetter("replicas")
    public Integer getReplicas() {
        return replicas;
    }

    @JsonGetter("config")
    public Map<String,String> getConfig() {
        return config;
    }

    @JsonGetter("consumerGroups")
    public List<String> getConsumerGroups() {
        return consumerGroups;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    @JsonGetter("topicDataURL")
    public String getTopicDataURL() {
        return topicDataURL;
    }

    @JsonGetter("schemaCatalog")
    public ExternalToolModel getSchemaCatalog() {
        return schemaCatalog;
    }

    @JsonGetter("grafanaMetrics")
    public ExternalToolModel getGrafanaMetrics() {
        return grafanaMetrics;
    }

    @JsonGetter("externalTools")
    public Collection<ExternalToolModel> getExternalTools() {
        return externalTools;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public void setConsumerGroups(List<String> consumerGroups) {
        this.consumerGroups = consumerGroups;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTopicDataURL(String topicDataURL) {
        this.topicDataURL = topicDataURL;
    }

    public void setSchemaCatalog(ExternalToolModel schemaCatalog) {
        this.schemaCatalog = schemaCatalog;
    }

    public void setGrafanaMetrics(ExternalToolModel grafanaMetrics) {
        this.grafanaMetrics = grafanaMetrics;
    }

    public void setExternalTools(Collection<ExternalToolModel> externalTools) {
        this.externalTools = externalTools;
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {
        TopicRecordModel record = new TopicRecordModel();

        public Builder name(String name) {
            record.setName(name);
            return this;
        }

        public Builder partitions(Integer partitions) {
            record.setPartitions(partitions);
            return this;
        }

        public Builder replicas(Integer replicas) {
            record.setReplicas(replicas);
            return this;
        }

        public Builder config(Map<String,String> config) {
            record.setConfig(config);
            return this;
        }

        public Builder consumerGroups(List<String> consumerGroups) {
            record.setConsumerGroups(consumerGroups);
            return this;
        }

        public Builder description(String description) {
            record.setDescription(description);
            return this;
        }

        public Builder topicDataURL(String topicDataURL) {
            record.setTopicDataURL(topicDataURL);
            return this;
        }

        public Builder schemaCatalog(ExternalToolModel schemaCatalog) {
            record.setSchemaCatalog(schemaCatalog);
            return this;
        }

        public Builder grafanaMetrics(ExternalToolModel grafanaMetrics) {
            record.setGrafanaMetrics(grafanaMetrics);
            return this;
        }

        public Builder externalTools(Collection<ExternalToolModel> externalTools) {
            record.setExternalTools(externalTools);
            return this;
        }

        public TopicRecordModel build() {
            return record;
        }

    }
}
