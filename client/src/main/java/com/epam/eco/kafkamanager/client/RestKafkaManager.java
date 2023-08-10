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
package com.epam.eco.kafkamanager.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.epam.eco.kafkamanager.BrokerConfigUpdateParams;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataDeleteParams;
import com.epam.eco.kafkamanager.BrokerMetadataUpdateParams;
import com.epam.eco.kafkamanager.BrokerSearchCriteria;
import com.epam.eco.kafkamanager.ConsumerGroupDeleteTopicParams;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataDeleteParams;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataUpdateParams;
import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.ConsumerGroupSearchCriteria;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.PermissionCreateParams;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.PrincipalPermissionsDeleteParams;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.ResourcePermissionsDeleteParams;
import com.epam.eco.kafkamanager.SearchCriteria;
import com.epam.eco.kafkamanager.TopicConfigUpdateParams;
import com.epam.eco.kafkamanager.TopicCreateParams;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataDeleteParams;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicPartitionsCreateParams;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicSearchCriteriaImpl;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionSearchCriteria;
import com.epam.eco.kafkamanager.rest.request.BrokerConfigRequest;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;
import com.epam.eco.kafkamanager.rest.request.PermissionRequest;
import com.epam.eco.kafkamanager.rest.request.TopicConfigRequest;
import com.epam.eco.kafkamanager.rest.request.TopicPartitionsRequest;
import com.epam.eco.kafkamanager.rest.request.TopicRequest;

/**
 * @author Raman_Babich
 */
@SuppressWarnings("deprecation")
public class RestKafkaManager implements KafkaManager {

    @Autowired
    @Qualifier("KafkaManagerRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private TopicRecordCounterTaskExecutor topicRecordCounterTaskExecutor;

    @Autowired
    private TopicOffsetRangeFetcherTaskExecutor topicOffsetRangeFetcherTaskExecutor;

    @Autowired
    private TopicOffsetForTimeFetcherTaskExecutor topicOffsetForTimeFetcherTaskExecutor;

    @Autowired
    private TopicPurgerTaskExecutor topicPurgerTaskExecutor;

    @Autowired
    private ConsumerGroupOffsetResetterTaskExecutor consumerGroupOffsetResetterTaskExecutor;

    @Autowired
    private ConsumerGroupTopicOffsetFetcherTaskExecutor consumerGroupTopicOffsetFetcherTaskExecutor;

    @Autowired
    private TopicRecordFetcherTaskExecutor<?, ?> topicRecordFetcherTaskExecutor;

    @Override
    public int getBrokerCount() {
        return (int) getBrokerPage(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public boolean brokerExists(int brokerId) {
        Map<String, Object> uriVariables = Collections.singletonMap("id", brokerId);

        try {
            restTemplate.headForHeaders(
                    "/api/brokers/{id}",
                    uriVariables);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }

    @Override
    public BrokerInfo getBroker(int brokerId) {
        Map<String, Object> uriVariables = Collections.singletonMap("id", brokerId);

        return restTemplate.getForObject(
                "/api/brokers/{id}",
                BrokerInfo.class,
                uriVariables);
    }

    @Override
    public List<BrokerInfo> getAllBrokers() {
        return getBrokerPage(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public List<BrokerInfo> getBrokers(BrokerSearchCriteria criteria) {
        return getBrokerPage(criteria, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public Page<BrokerInfo> getBrokerPage(Pageable pageable) {
        return getBrokerPage(BrokerSearchCriteria.builder().build(), pageable);
    }

    @Override
    public Page<BrokerInfo> getBrokerPage(BrokerSearchCriteria criteria, Pageable pageable) {
        Validate.notNull(criteria, "Criteria can't be null");
        Validate.notNull(pageable, "Pageable can't be null");

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("pageSize", pageable.getPageSize());
        params.put("brokerId", criteria.getBrokerId());
        params.put("rack", criteria.getRack());
        params.put("description", criteria.getDescription());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/brokers");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<PageImpl<BrokerInfo>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageImpl<BrokerInfo>>(){});

        Page<BrokerInfo> responsePage = response.getBody();
        return responsePage != null ? responsePage : Page.empty();
    }

    @Override
    public BrokerInfo updateBroker(BrokerMetadataUpdateParams params) {
        Validate.notNull(params, "BrokerMetadataUpdateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("id", params.getBrokerId());

        MetadataRequest request = new MetadataRequest(params.getDescription(), params.getAttributes());

        ResponseEntity<BrokerInfo> responseEntity = restTemplate.exchange(
                "/api/brokers/{id}/metadata",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                BrokerInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public BrokerInfo updateBroker(BrokerMetadataDeleteParams params) {
        Validate.notNull(params, "BrokerMetadataDeleteParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("id", params.getBrokerId());

        ResponseEntity<BrokerInfo> responseEntity = restTemplate.exchange(
                "/api/brokers/{id}/metadata",
                HttpMethod.DELETE,
                null,
                BrokerInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public BrokerInfo updateBroker(BrokerConfigUpdateParams params) {
        Validate.notNull(params, "BrokerConfigUpdateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("id", params.getBrokerId());

        BrokerConfigRequest request = new BrokerConfigRequest(params.getConfig());

        ResponseEntity<BrokerInfo> responseEntity = restTemplate.exchange(
                "/api/brokers/{id}/configs",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                BrokerInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public int getTopicCount() {
        return (int) getTopicPage(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public boolean topicExists(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", topicName);

        try {
            restTemplate.headForHeaders(
                    "/api/topics/{name}",
                    uriVariables);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }

    @Override
    public TopicInfo getTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", topicName);

        return restTemplate.getForObject(
                "/api/topics/{name}",
                TopicInfo.class,
                uriVariables);
    }

    @Override
    public List<TopicInfo> getAllTopics() {
        return getTopicPage(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public List<TopicInfo> getTopics(SearchCriteria<TopicInfo> criteria) {
        return getTopicPage(criteria, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public Page<TopicInfo> getTopicPage(Pageable pageable) {
        return getTopicPage(TopicSearchCriteriaImpl.builder().build(), pageable);
    }

    @Override
    public Page<TopicInfo> getTopicPage(SearchCriteria<TopicInfo> searchCriteria, Pageable pageable) {

        Validate.notNull(searchCriteria, "Criteria can't be null");
        Validate.notNull(pageable, "Pageable can't be null");

        TopicSearchCriteriaImpl criteria = (TopicSearchCriteriaImpl)searchCriteria;

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("pageSize", pageable.getPageSize());
        params.put("topicName", criteria.getTopicName());
        params.put("minPartitionCount", criteria.getMinPartitionCount());
        params.put("minReplicationFactor", criteria.getMinReplicationFactor());
        params.put("maxReplicationFactor", criteria.getMaxReplicationFactor());
        params.put("minConsumerCount", criteria.getMinConsumerCount());
        params.put("maxConsumerCount", criteria.getMaxConsumerCount());
        params.put("maxPartitionCount", criteria.getMaxPartitionCount());
        params.put("replicationState", criteria.getReplicationState());
        params.put("configString", criteria.getConfigString());
        params.put("configMap", criteria.getConfigMap());
        params.put("description", criteria.getDescription());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/topics");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<PageImpl<TopicInfo>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageImpl<TopicInfo>>(){});

        Page<TopicInfo> responsePage = response.getBody();
        return responsePage != null ? responsePage : Page.empty();
    }

    @Override
    public TopicInfo createTopic(TopicCreateParams params) {
        Validate.notNull(params, "TopicCreateParams object can't be null");

        TopicRequest request = new TopicRequest(
                params.getTopicName(),
                params.getPartitionCount(),
                params.getReplicationFactor(),
                params.getConfig(),
                params.getDescription(),
                params.getAttributes());

        ResponseEntity<TopicInfo> responseEntity = restTemplate.exchange(
                "/api/topics",
                HttpMethod.POST,
                new HttpEntity<>(request),
                TopicInfo.class,
                (Object) null);

        return responseEntity.getBody();
    }

    @Override
    public TopicInfo updateTopic(TopicConfigUpdateParams params) {
        Validate.notNull(params, "TopicConfigUpdateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getTopicName());

        TopicConfigRequest request = new TopicConfigRequest(params.getConfig());

        ResponseEntity<TopicInfo> responseEntity = restTemplate.exchange(
                "/api/topics/{name}/configs",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                TopicInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public TopicInfo updateTopic(TopicPartitionsCreateParams params) {
        Validate.notNull(params, "TopicPartitionsCreateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getTopicName());

        TopicPartitionsRequest request = new TopicPartitionsRequest(params.getNewPartitionCount());

        ResponseEntity<TopicInfo> responseEntity = restTemplate.exchange(
                "/api/topics/{name}/partitions",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                TopicInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public TopicInfo updateTopic(TopicMetadataUpdateParams params) {
        Validate.notNull(params, "TopicMetadataUpdateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getTopicName());

        MetadataRequest request = new MetadataRequest(
                params.getDescription(),
                params.getAttributes());

        ResponseEntity<TopicInfo> responseEntity = restTemplate.exchange(
                "/api/topics/{name}/metadata",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                TopicInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public TopicInfo updateTopic(TopicMetadataDeleteParams params) {
        Validate.notNull(params, "TopicMetadataDeleteParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getTopicName());

        ResponseEntity<TopicInfo> responseEntity = restTemplate.exchange(
                "/api/topics/{name}/metadata",
                HttpMethod.DELETE,
                null,
                TopicInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public void deleteTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", topicName);

        restTemplate.delete(
                "/api/topics/{name}",
                uriVariables);
    }

    @Override
    public TopicRecordCounterTaskExecutor getTopicRecordCounterTaskExecutor() {
        return topicRecordCounterTaskExecutor;
    }

    @Deprecated
    @Override
    public TopicOffsetFetcherTaskExecutor getTopicOffsetFetcherTaskExecutor() {
        return topicOffsetRangeFetcherTaskExecutor;
    }

    @Override
    public TopicOffsetRangeFetcherTaskExecutor getTopicOffsetRangeFetcherTaskExecutor() {
        return topicOffsetRangeFetcherTaskExecutor;
    }

    @Override
    public TopicOffsetForTimeFetcherTaskExecutor getTopicOffsetForTimeFetcherTaskExecutor() {
        return topicOffsetForTimeFetcherTaskExecutor;
    }

    @Override
    public TopicPurgerTaskExecutor getTopicPurgerTaskExecutor() {
        return topicPurgerTaskExecutor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> TopicRecordFetcherTaskExecutor<K, V> getTopicRecordFetcherTaskExecutor() {
        return (TopicRecordFetcherTaskExecutor<K, V>) topicRecordFetcherTaskExecutor;
    }

    @Override
    public int getConsumerGroupCount() {
        return (int) getConsumerGroupPage(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public boolean consumerGroupExists(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", groupName);

        try {
            restTemplate.headForHeaders(
                    "/api/consumer-groups/{name}",
                    uriVariables);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }

    @Override
    public ConsumerGroupInfo getConsumerGroup(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", groupName);

        return restTemplate.getForObject(
                "/api/consumer-groups/{name}",
                ConsumerGroupInfo.class,
                uriVariables);
    }

    @Override
    public List<ConsumerGroupInfo> getAllConsumerGroups() {
        return getConsumerGroupPage(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public List<ConsumerGroupInfo> getConsumerGroups(SearchCriteria<ConsumerGroupInfo> criteria) {
        return getConsumerGroupPage(criteria, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public Page<ConsumerGroupInfo> getConsumerGroupPage(Pageable pageable) {
        return getConsumerGroupPage(ConsumerGroupSearchCriteria.builder().build(), pageable);
    }

    @Override
    public Page<ConsumerGroupInfo> getConsumerGroupPage(SearchCriteria<ConsumerGroupInfo> searchCriteria, Pageable pageable) {
        Validate.notNull(searchCriteria, "Criteria can't be null");
        Validate.notNull(pageable, "Pageable can't be null");

        Validate.isTrue(searchCriteria instanceof ConsumerGroupSearchCriteria, "Wrong type of search criteria");

        ConsumerGroupSearchCriteria criteria = (ConsumerGroupSearchCriteria)searchCriteria;

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("pageSize", pageable.getPageSize());
        params.put("groupName", criteria.getGroupName());
        params.put("storageType", criteria.getStorageType());
        params.put("description", criteria.getDescription());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/consumer-groups");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<PageImpl<ConsumerGroupInfo>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageImpl<ConsumerGroupInfo>>(){});

        Page<ConsumerGroupInfo> responsePage = response.getBody();
        return responsePage != null ? responsePage : Page.empty();
    }

    @Override
    public List<ConsumerGroupInfo> getConsumerGroupsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("topicName", topicName);

        ResponseEntity<List<ConsumerGroupInfo>> responseEntity = restTemplate.exchange(
                "/api/topics/{topicName}/consumer-groups",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ConsumerGroupInfo>>() {},
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupDeleteTopicParams params) {
        Validate.notNull(params, "ConsumerGroupDeleteTopicParams object can't be null");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("groupName", params.getGroupName());
        uriVariables.put("topicName", params.getTopicName());

        ResponseEntity<ConsumerGroupInfo> responseEntity = restTemplate.exchange(
                "/api/consumer-groups/{groupName}/topics/{topicName}",
                HttpMethod.DELETE,
                null,
                ConsumerGroupInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataUpdateParams params) {
        Validate.notNull(params, "ConsumerGroupMetadataUpdateParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getGroupName());

        MetadataRequest request = new MetadataRequest(
                params.getDescription(),
                params.getAttributes());

        ResponseEntity<ConsumerGroupInfo> responseEntity = restTemplate.exchange(
                "/api/consumer-groups/{name}/metadata",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                ConsumerGroupInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataDeleteParams params) {
        Validate.notNull(params, "ConsumerGroupMetadataDeleteParams object can't be null");

        Map<String, Object> uriVariables = Collections.singletonMap("name", params.getGroupName());

        ResponseEntity<ConsumerGroupInfo> responseEntity = restTemplate.exchange(
                "/api/consumer-groups/{name}/metadata",
                HttpMethod.DELETE,
                null,
                ConsumerGroupInfo.class,
                uriVariables);

        return responseEntity.getBody();
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("name", groupName);

        restTemplate.delete(
                "/api/consumer-groups/{name}",
                uriVariables);
    }

    @Override
    public ConsumerGroupOffsetResetterTaskExecutor getConsumerGroupOffsetResetterTaskExecutor() {
        return consumerGroupOffsetResetterTaskExecutor;
    }

    @Override
    public ConsumerGroupTopicOffsetFetcherTaskExecutor getConsumerGroupTopicOffsetFetcherTaskExecutor() {
        return consumerGroupTopicOffsetFetcherTaskExecutor;
    }

    @Override
    public int getPermissionCount() {
        return (int) getPermissionPage(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public List<PermissionInfo> getAllPermissions() {
        return getPermissionPage(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public List<PermissionInfo> getPermissions(PermissionSearchCriteria criteria) {
        return getPermissionPage(criteria, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public Page<PermissionInfo> getPermissionPage(Pageable pageable) {
        return getPermissionPage(PermissionSearchCriteria.builder().build(), pageable);
    }

    @Override
    public Page<PermissionInfo> getPermissionPage(PermissionSearchCriteria criteria, Pageable pageable) {
        Validate.notNull(criteria, "Criteria can't be null");
        Validate.notNull(pageable, "Pageable can't be null");

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("pageSize", pageable.getPageSize());
        params.put("resourceName", criteria.getResourceName());
        params.put("resourceType", criteria.getResourceType());
        params.put("kafkaPrincipal", criteria.getKafkaPrincipal());
        params.put("host", criteria.getHost());
        params.put("operation", criteria.getOperation());
        params.put("permissionType", criteria.getPermissionType());
        params.put("description", criteria.getDescription());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/permissions");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<PageImpl<PermissionInfo>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageImpl<PermissionInfo>>(){});

        Page<PermissionInfo> responsePage = response.getBody();
        return responsePage != null ? responsePage : Page.empty();
    }

    @Override
    public List<PermissionInfo> getPermissionsOfResource(ResourcePermissionFilter filter) {
        Validate.notNull(filter, "Filter can't be null");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/permissions/resource");
        builder.path(filter.getResourceType().name());
        builder.path(filter.getResourceName());
        builder.path(filter.getPatternType().name());

        return restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PermissionInfo>>(){}).getBody();
    }

    @Override
    public void createPermission(PermissionCreateParams params) {
        Validate.notNull(params, "PermissionCreateParams object can't be null");

        PermissionRequest request = new PermissionRequest(
                params.getResourceType(),
                params.getResourceName(),
                params.getPatternType(),
                params.getPrincipal(),
                params.getPermissionType(),
                params.getOperation(),
                params.getHost());

        restTemplate.postForLocation(
                "/api/permissions",
                request);
    }

    @Override
    public void updatePermission(PermissionMetadataUpdateParams params) {
        Validate.notNull(params, "PermissionMetadataUpdateParams object can't be null");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("resourceName", params.getResourceName());
        uriVariables.put("resourceType", params.getResourceType());
        uriVariables.put("patternType", params.getPatternType());
        uriVariables.put("principal", params.getPrincipal());

        MetadataRequest request = new MetadataRequest(
                params.getDescription(),
                params.getAttributes());

        restTemplate.put(
                "/api/permissions/resource/{resourceType}/{resourceName}/{patternType}/metadata?principal={principal}",
                request,
                uriVariables);
    }

    @Override
    public void updatePermission(PermissionMetadataDeleteParams params) {
        Validate.notNull(params, "PermissionMetadataDeleteParams object can't be null");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("resourceName", params.getResourceName());
        uriVariables.put("resourceType", params.getResourceType());
        uriVariables.put("patternType", params.getPatternType());
        uriVariables.put("principal", params.getPrincipal());

        restTemplate.delete(
                "/api/permissions/resource/{resourceType}/{resourceName}/{patternType}/metadata?principal={principal}",
                uriVariables);
    }

    @Override
    public void deletePermissions(ResourcePermissionsDeleteParams params) {
        Validate.notNull(params, "ResourcePermissionsDeleteParams object can't be null");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("resourceName", params.getFilter().getResourceName());
        uriVariables.put("resourceType", params.getFilter().getResourceType());
        uriVariables.put("patternType", params.getFilter().getPatternType());
        uriVariables.put("principalFilter", params.getFilter().getPrincipalFilter());
        uriVariables.put("permissionTypeFilter", params.getFilter().getPermissionTypeFilter());
        uriVariables.put("operationFilter", params.getFilter().getOperationFilter());
        uriVariables.put("hostFilter", params.getFilter().getHostFilter());

        restTemplate.delete(
                "/api/permissions/resource/{resourceType}/{resourceName}/{patternType}" +
                "?principalFilter={principalFilter}" +
                "&permissionTypeFilter={permissionTypeFilter}" +
                "&operationFilter={operationFilter}" +
                "$hostFilter={hostFilter}",
                uriVariables);
    }

    @Override
    public void deletePermissions(PrincipalPermissionsDeleteParams params) {
        Validate.notNull(params, "PrincipalPermissionsDeleteParams object can't be null");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("principal", params.getPrincipal());

        restTemplate.delete(
                "/api/permissions/principal?principal={principal}",
                uriVariables);
    }

    @Override
    public int getTransactionCount() {
        return (int) getTransactionPage(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public boolean transactionExists(String transactionalId) {
        Validate.notBlank(transactionalId, "Transaction id can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("transactionId", transactionalId);

        try {
            restTemplate.headForHeaders(
                    "/api/transactions/{transactionId}",
                    uriVariables);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }

    @Override
    public TransactionInfo getTransaction(String transactionalId) {
        Validate.notBlank(transactionalId, "Transaction id can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("transactionId", transactionalId);

        return restTemplate.getForObject(
                "/api/transactions/{transactionId}",
                TransactionInfo.class,
                uriVariables);
    }

    @Override
    public List<TransactionInfo> getAllTransactions() {
        return getTransactionPage(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public List<TransactionInfo> getTransactions(TransactionSearchCriteria criteria) {
        return getTransactionPage(criteria, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Override
    public Page<TransactionInfo> getTransactionPage(Pageable pageable) {
        return getTransactionPage(TransactionSearchCriteria.builder().build(), pageable);
    }

    @Override
    public Page<TransactionInfo> getTransactionPage(
            TransactionSearchCriteria criteria,
            Pageable pageable) {
        Validate.notNull(criteria, "Criteria can't be null");
        Validate.notNull(pageable, "Pageable can't be null");

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("pageSize", pageable.getPageSize());
        params.put("transactionId", criteria.getTransactionalId());
        params.put("topicName", criteria.getTopicName());
        params.put("state", criteria.getState());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/transactions");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<PageImpl<TransactionInfo>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageImpl<TransactionInfo>>(){});

        Page<TransactionInfo> responsePage = response.getBody();
        return responsePage != null ? responsePage : Page.empty();
    }

    @Override
    public List<TransactionInfo> getTransactionsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        Map<String, Object> uriVariables = Collections.singletonMap("topicName", topicName);

        ResponseEntity<List<TransactionInfo>> responseEntity = restTemplate.exchange(
                "/api/topics/{topicName}/transactions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TransactionInfo>>() {},
                uriVariables);

        return responseEntity.getBody();
    }

}
