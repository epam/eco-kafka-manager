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
package com.epam.eco.kafkamanager.ui.topics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epam.eco.commons.kafka.config.TopicConfigDef;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.ReplicationState;
import com.epam.eco.kafkamanager.TopicConfigUpdateParams;
import com.epam.eco.kafkamanager.TopicCreateParams;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicListSearchCriteria;
import com.epam.eco.kafkamanager.TopicMetadataDeleteParams;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicPartitionsCreateParams;
import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricManager;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;
import com.epam.eco.kafkamanager.ui.metrics.udm.UDMetricWrapper;
import com.epam.eco.kafkamanager.ui.topics.export.TopicExporterType;
import com.epam.eco.kafkamanager.ui.topics.model.TopicInfoToModelMapper;
import com.epam.eco.kafkamanager.ui.topics.model.TopicTableModel;
import com.epam.eco.kafkamanager.ui.utils.ComboBoxModel;
import com.epam.eco.kafkamanager.ui.utils.MetadataWrapper;
import com.epam.eco.kafkamanager.utils.MapperUtils;

import static com.epam.eco.kafkamanager.ui.topics.browser.TopicBrowserController.ATTR_SCHEMA_CATALOG_URL_TEMPLATE;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TopicController {

    public static final String TOPICS_VIEW = "topics";
    public static final String TOPIC_VIEW = "topic";
    public static final String TOPIC_CREATE_VIEW = "topic_create";
    public static final String TOPIC_CONFIG_UPDATE_VIEW = "topic_config_update";
    public static final String TOPIC_PARTITIONS_CREATE_VIEW = "topic_partitions_create";
    public static final String TOPIC_METADATA_VIEW = "topic_metadata";
    public static final String ATTR_TOPIC = "topic";
    public static final String ATTR_CONFIG_DEF = "configDef";
    public static final String ATTR_TOPIC_OFFSET_INCREASE_UDM_TYPE = "topicOffsetIncreaseUdmType";
    public static final String ATTR_TOPIC_OFFSET_INCREASE_UDM_NAME = "topicOffsetIncreaseUdmName";
    public static final String ATTR_TOPIC_OFFSET_INCREASE_UDM = "topicOffsetIncreaseUdm";
    public static final String ATTR_DEFAULT_PARTITION_COUNT = "defaultPartitionCount";
    public static final String ATTR_DEFAULT_REPLICATION_FACTOR = "defaultReplicationFactor";
    public static final String ATTR_MAX_REPLICATION_FACTOR = "maxReplicationFactor";
    public static final String ATTR_REPLICATION_STATES = "replicationStates";
    public static final String ATTR_METADATA = "metadata";
    public static final String ATTR_DATA_CATALOG_URL_TEMPLATE = "dataCatalogUrlTemplate";
    public static final String ATTR_GRAFANA_METRICS_URL_TEMPLATE = "grafanaMetricsUrlTemplate";
    public static final String ATTR_EXTERNAL_TOOL_TEMPLATES = "externalToolTemplates";
    public static final String ATTR_FULL_SCREEN = "fullScreen";

    public static final String MAPPING_TOPICS = "/topics";
    public static final String MAPPING_TOPIC_LIST = "/topic_list";
    public static final String MAPPING_TOPIC = MAPPING_TOPICS + "/{name}";
    public static final String MAPPING_COUNT_RECORDS = MAPPING_TOPIC + "/count_records";
    public static final String MAPPING_PURGER = MAPPING_TOPIC + "/purger";
    public static final String MAPPING_DELETE = MAPPING_TOPIC + "/delete";
    public static final String MAPPING_CONFIG = MAPPING_TOPIC + "/config";
    public static final String MAPPING_PARTITIONS = MAPPING_TOPIC + "/partitions";
    public static final String MAPPING_METADATA = MAPPING_TOPIC + "/metadata";
    public static final String MAPPING_EXPORT = "/topics_export";
    public static final String MAPPING_CREATE = "/topic_create";

    @Autowired
    private KafkaAdminOperations kafkaAdminOperations;
    @Autowired
    private KafkaManagerUiProperties properties;
    @Autowired
    private KafkaManager kafkaManager;
    @Autowired(required=false)
    private UDMetricManager udMetricManager;

    public static Map<String, String> extractConfigsFromParams(
            Map<String, String> paramsMap,
            boolean skipNulls) {
        Map<String, String> configs = new HashMap<>((int) Math.ceil(paramsMap.size() / 0.75));
        for(Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TopicConfigDef.INSTANCE.key(key) == null || (skipNulls && StringUtils.isBlank(value))) {
                continue;
            }
            configs.put(key, StringUtils.stripToNull(value));
        }
        return configs;
    }

    public static String buildTopicUrl(String topicName) {
        return MAPPING_TOPIC.replace("{name}", topicName);
    }

    @RequestMapping(value=MAPPING_TOPICS, method = RequestMethod.GET)
    public String topics(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {

        model.addAttribute(ATTR_DATA_CATALOG_URL_TEMPLATE, properties.getDataCatalogTool());
        model.addAttribute(ATTR_GRAFANA_METRICS_URL_TEMPLATE, properties.getGrafanaMetrics());
        model.addAttribute(ATTR_REPLICATION_STATES,
                           Arrays.stream(ReplicationState.values())
                                                          .map(state->ComboBoxModel.build(state.name()))
                                                          .collect(Collectors.toList()));

        model.addAttribute(ATTR_EXTERNAL_TOOL_TEMPLATES, properties.getExternalTools());

        model.addAttribute(ATTR_FULL_SCREEN, paramsMap.get(ATTR_FULL_SCREEN));

        return TOPICS_VIEW;
    }

    @RequestMapping(value=MAPPING_TOPIC_LIST, method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<TopicTableModel> topicList(
            @RequestParam Map<String, Object> paramsMap) {
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(paramsMap, kafkaManager);
        List<TopicInfo> topics = kafkaManager.getTopics(criteria);
        TopicInfoToModelMapper mapper =
                new TopicInfoToModelMapper(properties.getDataCatalogTool(),
                                           properties.getGrafanaMetrics(),
                                           kafkaManager,
                                           properties.getExternalTools());
        TopicTableModel model = TopicTableModel.builder()
                .draw(1)
                .recordsTotal(topics.size())
                .recordsFiltered(topics.size())
                .data(topics.stream()
                            .map(mapper)
                            .collect(Collectors.toList()) )
                .build();
        return ResponseEntity.ok(model);
    }

    @RequestMapping(value = MAPPING_TOPIC, method = RequestMethod.GET)
    public String topic(@PathVariable("name") String topicName, Model model) {
        model.addAttribute(
                ATTR_TOPIC,
                TopicInfoWrapper.wrap(topicName, kafkaManager, kafkaAdminOperations));

        String topicOffsetIncreaseUdmName = UDMetricType.TOPIC_OFFSET_INCREASE.formatName(topicName);
        model.addAttribute(ATTR_TOPIC_OFFSET_INCREASE_UDM_TYPE, UDMetricType.TOPIC_OFFSET_INCREASE);
        model.addAttribute(ATTR_TOPIC_OFFSET_INCREASE_UDM_NAME, topicOffsetIncreaseUdmName);
        model.addAttribute(ATTR_TOPIC_OFFSET_INCREASE_UDM, getAndWrapUdm(topicOffsetIncreaseUdmName));

        model.addAttribute(ATTR_DATA_CATALOG_URL_TEMPLATE, properties.getDataCatalogTool());
        model.addAttribute(ATTR_GRAFANA_METRICS_URL_TEMPLATE, properties.getGrafanaMetrics());
        model.addAttribute(ATTR_SCHEMA_CATALOG_URL_TEMPLATE, properties.getSchemaCatalogTool() );
        model.addAttribute(ATTR_EXTERNAL_TOOL_TEMPLATES, properties.getExternalTools());

        return TOPIC_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING_COUNT_RECORDS, method=RequestMethod.GET)
    public @ResponseBody ResponseEntity<?> countRecords(
            @PathVariable("name") String topicName) throws Exception {
        Future<Long> future = kafkaManager.getTopicRecordCounterTaskExecutor().submit(topicName);
        return ResponseEntity.ok(future.get(30, TimeUnit.SECONDS));
    }

    @RequestMapping(value=MAPPING_EXPORT, method=RequestMethod.GET)
    public void export(
            @RequestParam TopicExporterType exporterType,
            @RequestParam Map<String, Object> paramsMap,
            HttpServletResponse response) throws IOException {

        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(paramsMap, kafkaManager);
        List<TopicInfo> topicInfos = kafkaManager.getTopics(criteria);

        response.setContentType(exporterType.contentType());
        response.setHeader(
                "Content-Disposition",
                String.format(
                        "attachment; filename=\"%s_%d.txt\"",
                        exporterType.name(), System.currentTimeMillis()));

        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            exporterType.exporter().export(topicInfos, out);
            out.flush();
        }
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'WRITE')")
    @RequestMapping(value=MAPPING_PURGER, method=RequestMethod.POST)
    public String purger(@PathVariable("name") String topicName) throws Exception {
        kafkaManager.getTopicPurgerTaskExecutor().execute(topicName);
        return "redirect:" + buildTopicUrl(topicName);
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'DELETE')")
    @RequestMapping(value=MAPPING_DELETE, method=RequestMethod.POST)
    public String delete(@PathVariable("name") String topicName) {
        kafkaManager.deleteTopic(topicName);
        return "redirect:" + MAPPING_TOPICS;
    }

    @RequestMapping(value=MAPPING_CREATE, method=RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute(ATTR_DEFAULT_PARTITION_COUNT, 1);
        model.addAttribute(ATTR_DEFAULT_REPLICATION_FACTOR, kafkaAdminOperations.getDefaultReplicationFactor());
        model.addAttribute(ATTR_MAX_REPLICATION_FACTOR, kafkaManager.getBrokerCount());
        model.addAttribute(ATTR_CONFIG_DEF, TopicConfigDef.INSTANCE);

        return TOPIC_CREATE_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #paramsMap.get('topicName'), 'CREATE')")
    @RequestMapping(value=MAPPING_CREATE, method=RequestMethod.POST)
    public String create(
            @RequestParam String topicName,
            @RequestParam Integer partitionCount,
            @RequestParam Integer replicationFactor,
            @RequestParam(required=false) String description,
            @RequestParam(required=false) String attributes,
            @RequestParam Map<String, String> paramsMap) {
        TopicCreateParams params = TopicCreateParams.builder().
                topicName(topicName).
                partitionCount(partitionCount).
                replicationFactor(replicationFactor).
                config(extractConfigsFromParams(paramsMap, true)).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build();

        TopicInfo topicInfo = kafkaManager.createTopic(params);
        return "redirect:" + buildTopicUrl(topicInfo.getName());
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'ALTER_CONFIG')")
    @RequestMapping(value=MAPPING_CONFIG, method = RequestMethod.GET)
    public String config(@PathVariable("name") String topicName, Model model) {
        model.addAttribute(
                ATTR_TOPIC,
                TopicInfoWrapper.wrap(topicName, kafkaManager, kafkaAdminOperations));

        return TOPIC_CONFIG_UPDATE_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #paramsMap.get('topicName'), 'ALTER_CONFIG')")
    @RequestMapping(value=MAPPING_CONFIG, method=RequestMethod.POST)
    public String config(
            @RequestParam String topicName,
            @RequestParam Map<String, String> paramsMap) {
        TopicConfigUpdateParams params = TopicConfigUpdateParams.builder().
                topicName(topicName).
                config(extractConfigsFromParams(paramsMap, false)).
                build();

        TopicInfo topicInfo = kafkaManager.updateTopic(params);
        return "redirect:" + buildTopicUrl(topicInfo.getName());
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'ALTER')")
    @RequestMapping(value=MAPPING_PARTITIONS, method = RequestMethod.GET)
    public String partitions(@PathVariable("name") String topicName, Model model) {
        model.addAttribute(
                ATTR_TOPIC,
                TopicInfoWrapper.wrap(topicName, kafkaManager, kafkaAdminOperations));

        return TOPIC_PARTITIONS_CREATE_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #paramsMap.get('topicName'), 'ALTER')")
    @RequestMapping(value=MAPPING_PARTITIONS, method=RequestMethod.POST)
    public String partitions(@RequestParam Map<String, Object> paramsMap) {
        TopicPartitionsCreateParams params = TopicPartitionsCreateParams.fromJson(paramsMap);

        TopicInfo topicInfo = kafkaManager.updateTopic(params);
        return "redirect:" + buildTopicUrl(topicInfo.getName());
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.GET)
    public String metadata(@PathVariable("name") String topicName, Model model) {
        TopicInfo topicInfo = kafkaManager.getTopic(topicName);
        model.addAttribute(ATTR_TOPIC, TopicInfoWrapper.wrap(topicInfo, kafkaManager, kafkaAdminOperations));
        if (topicInfo.getMetadata().isPresent()) {
            model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(topicInfo.getMetadata().get()));
        }
        return TOPIC_METADATA_VIEW;
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.POST)
    public String metadata(String topicName, String description, String attributes) {
        kafkaManager.updateTopic(
                TopicMetadataUpdateParams.builder().
                topicName(topicName).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build());
        return "redirect:" + buildTopicUrl(topicName);
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.DELETE)
    public String metadata(@PathVariable("name") String topicName) {
        kafkaManager.updateTopic(
                TopicMetadataDeleteParams.builder().
                topicName(topicName).
                build());
        return "redirect:" + buildTopicUrl(topicName);
    }

    private UDMetricWrapper getAndWrapUdm(String udmName) {
        if (udMetricManager == null) {
            return null;
        }

        UDMetric udm = udMetricManager.get(udmName);
        return udm != null ? UDMetricWrapper.wrap(udm) : null;
    }

}
