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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.ui.config.DataCatalogUrlTemplate;
import com.epam.eco.kafkamanager.ui.config.ExternalToolTemplate;
import com.epam.eco.kafkamanager.ui.config.GrafanaUrlTemplate;

import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class TopicInfoToModelMapper implements Function<TopicInfo, TopicRecordModel> {

    private final DataCatalogUrlTemplate dataCatalogUrlTemplate;
    private final GrafanaUrlTemplate grafanaUrlTemplate;
    private final Collection<ExternalToolTemplate> externalToolTemplates;

    private final KafkaManager kafkaManager;

    public TopicInfoToModelMapper(DataCatalogUrlTemplate dataCatalogUrlTemplate,
                                  GrafanaUrlTemplate grafanaUrlTemplate,
                                  KafkaManager kafkaManager,
                                  Collection<ExternalToolTemplate> externalToolTemplates) {
        this.dataCatalogUrlTemplate = dataCatalogUrlTemplate;
        this.grafanaUrlTemplate = grafanaUrlTemplate;
        this.externalToolTemplates = externalToolTemplates;
        this.kafkaManager = kafkaManager;
    }

    @Override
    public TopicRecordModel apply(TopicInfo topicInfo) {

        TopicRecordModel model = TopicRecordModel.builder()
                        .name(topicInfo.getName())
                        .partitions(topicInfo.getPartitionCount())
                        .replicas(topicInfo.getReplicationFactor())
                        .config(topicInfo.getConfig())
                        .topicDataURL(topicInfo.getName())
                        .build();

        topicInfo.getMetadata().ifPresent(value -> model.setDescription(value.getDescription()));

        if(nonNull(dataCatalogUrlTemplate)) {
            model.setSchemaCatalog(ExternalToolModel.builder()
                                           .url(dataCatalogUrlTemplate.resolve(topicInfo.getName()))
                                           .toolName(dataCatalogUrlTemplate.getName())
                                           .icon(dataCatalogUrlTemplate.getIcon())
                                           .build());
        }
        if(nonNull(grafanaUrlTemplate)) {
            model.setGrafanaMetrics(ExternalToolModel.builder()
                                                     .url(grafanaUrlTemplate.resolve(topicInfo.getName()))
                                                     .toolName(grafanaUrlTemplate.getName())
                                                     .icon(grafanaUrlTemplate.getIcon())
                                                     .build());
        }
        if(nonNull(externalToolTemplates) && externalToolTemplates.size()>0) {
            Collection<ExternalToolModel> externalToolModels = new LinkedList<>();
            externalToolTemplates.forEach(externalToolTemplate -> {
                externalToolModels.add(ExternalToolModel.builder()
                                           .url(externalToolTemplate.resolve(topicInfo.getName()))
                                           .toolName(externalToolTemplate.getName())
                                           .icon(externalToolTemplate.getIcon())
                                           .build() );

            });
            model.setExternalTools(externalToolModels);
        }
        List<ConsumerGroupInfo> consumerGroupInfos = kafkaManager.getConsumerGroupsForTopic(topicInfo.getName());
        List<String> consumerGroups = new LinkedList<>();
        if(nonNull(consumerGroupInfos) && !consumerGroupInfos.isEmpty()) {
            consumerGroupInfos.forEach(consumerGroupInfo -> consumerGroups.add(consumerGroupInfo.getName()));
        }
        model.setConsumerGroups(consumerGroups);

        return model;
    }
}
