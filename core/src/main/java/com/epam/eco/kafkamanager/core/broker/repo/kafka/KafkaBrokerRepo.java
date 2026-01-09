/*******************************************************************************
 *  Copyright 2025 EPAM Systems
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
package com.epam.eco.kafkamanager.core.broker.repo.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.server.common.MetadataVersion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataKey;
import com.epam.eco.kafkamanager.BrokerRepo;
import com.epam.eco.kafkamanager.BrokerSearchCriteria;
import com.epam.eco.kafkamanager.EndPointInfo;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.core.utils.ExceptionUtils;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import static com.epam.eco.commons.kafka.AdminClientUtils.configToMap;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class KafkaBrokerRepo extends AbstractKeyValueRepo<Integer, BrokerInfo, BrokerSearchCriteria>
        implements BrokerRepo, CachedRepo<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBrokerRepo.class);
    private static final String LISTENER_SECURITY_PROTOCOL_MAP = "listener.security.protocol.map";
    private static final String ADVERTISED_LISTENERS = "advertised.listeners";
    private static final String METADATA_VERSION = "metadata.version";
    @Autowired
    private KafkaAdminOperations adminOperations;

    @Autowired
    private MetadataRepo metadataRepo;

    @Override
    public int size() {
        return adminOperations.describeCluster().size();
    }

    @Override
    public boolean contains(Integer brokerId) {
        Validate.notNull(brokerId, "Broker id is null");
        Validate.isTrue(brokerId >= 0, "Broker id is invalid");

        return adminOperations.describeCluster().stream()
                .anyMatch(node -> node.id() == brokerId);
    }

    @Override
    public BrokerInfo get(Integer brokerId) {
        Validate.notNull(brokerId, "Broker id is null");
        Validate.isTrue(brokerId >= 0, "Broker id is invalid");

        return describeBrokerById(brokerId);
    }

    @Override
    public List<BrokerInfo> values() {
        LOGGER.debug("Fetching all brokers");
        Map<Integer, Node> nodes =
                adminOperations.describeCluster().stream().collect(toMap(Node::id,
                        Function.identity()));
        return getBrokerInfos(nodes.keySet().stream().toList(), nodes);
    }

    @Override
    public List<BrokerInfo> values(List<Integer> brokerIds) {
        Validate.noNullElements(brokerIds, "Collection of broker ids can't be null or contain " +
                "null elements");
        Map<Integer, Node> nodes =
                adminOperations.describeCluster().stream()
                        .filter(node -> brokerIds.contains(node.id()))
                        .collect(toMap(Node::id,
                        Function.identity()));
        return getBrokerInfos(brokerIds, nodes);
    }

    @NotNull
    private List<BrokerInfo> getBrokerInfos(
            List<Integer> brokerIds,
            Map<Integer, Node> nodes
    ) {

        Map<Integer, Config> configs = adminOperations.describeBrokerConfigs(brokerIds);

        return nodes.entrySet().stream()
                .map(entry ->
                        createBrokerInfo(entry.getValue(), extractBrokersVersion(),
                                configToMap(configs.get(entry.getKey()), false, false)))
                .toList();
    }

    @Override
    public List<Integer> keys() {
        return adminOperations.describeCluster().stream()
                .map(Node::id)
                .collect(Collectors.toList());
    }

    @Override
    public void evict(Integer brokerId) {
        // No-op since we're not caching anymore
    }

    @Override
    public BrokerInfo updateConfig(
            int brokerId,
            Map<String, String> configs
    ) {
        Validate.isTrue(brokerId >= 0, "Broker id is invalid: %d", brokerId);
        Validate.notNull(configs, "Map of configs is null");

        if (!contains(brokerId)) {
            throw new NotFoundException(String.format("Broker %d doesn't exist", brokerId));
        }

        adminOperations.alterBrokerConfigs(brokerId, configs);

        return describeBrokerById(brokerId);
    }

    private String extractBrokersVersion() {
        String metadataVersion =
                ExceptionUtils.doQuietly(
                        () -> MetadataVersion.fromFeatureLevel(adminOperations.describeFeatures()
                                .finalizedFeatures()
                                .get(METADATA_VERSION).maxVersionLevel()).version()
                );
        return Objects.toString(metadataVersion, EMPTY);
    }

    private Map<String, String> parseListenerSecurityProtocolMap(String listenerMap) {
        if (listenerMap == null || listenerMap.trim().isEmpty()) {
            return emptyMap();
        }

        return Arrays.stream(listenerMap.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(entry -> entry.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(toMap(
                        parts -> parts[0],
                        parts -> parts[1],
                        (existing, replacement) -> replacement
                ));
    }

    private List<EndPointInfo> toEndPointInfo(
            String listeners,
            Map<String, String> protocolsMap
    ) {
        return Arrays.stream(listeners.split(",")).
                map(endPoint -> {
                            String[] items = endPoint.split(":");
                            if (items.length == 3) {
                                String protocol =
                                        protocolsMap.get(items[0]);
                                if (protocol != null) {
                                    return new EndPointInfo(
                                            SecurityProtocol.valueOf(protocol),
                                            items[1].replace("//", EMPTY),
                                            Integer.parseInt(items[2]));

                                }
                            }
                            return null;
                        }
                ).toList();
    }


    private BrokerInfo createBrokerInfo(
            Node node,
            String version,
            Map<String, String> config
    ) {
        Map<String, String> protocolsMap =
                parseListenerSecurityProtocolMap(config.get(LISTENER_SECURITY_PROTOCOL_MAP));
        String listeners = config.get(ADVERTISED_LISTENERS);
        List<EndPointInfo> endpoints = toEndPointInfo(listeners, protocolsMap);

        return BrokerInfo.builder()
                .id(node.id())
                .endPoints(endpoints)
                .rack(node.rack())
                .config(config)
                .version(version)
                .metadata(metadataRepo.get(BrokerMetadataKey.with(node.id())))
                .build();
    }

    private BrokerInfo describeBrokerById(Integer brokerId) {
        Config config = adminOperations.describeBrokerConfig(brokerId);
        if (config == null) {
            LOGGER.warn("Config for brokerId not found.");
            throw new NotFoundException(String.format("Broker not found by id %d", brokerId));
        }

        Node node = adminOperations.describeCluster().stream()
                .filter(n -> n.id() == brokerId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Broker %d not found",
                        brokerId)));

        return createBrokerInfo(node, extractBrokersVersion(), configToMap(config, false, false));
    }

}
