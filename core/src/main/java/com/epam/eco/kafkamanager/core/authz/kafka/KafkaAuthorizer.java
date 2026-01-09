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
package com.epam.eco.kafkamanager.core.authz.kafka;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.util.StopWatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.ClusterResource;
import org.apache.kafka.common.Endpoint;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.Monitorable;
import org.apache.kafka.common.metrics.internals.PluginMetricsImpl;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.metadata.authorizer.ClusterMetadataAuthorizer;
import org.apache.kafka.metadata.authorizer.StandardAcl;
import org.apache.kafka.server.authorizer.Action;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.authorizer.AuthorizationResult;
import org.apache.kafka.server.authorizer.AuthorizerServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.Authorizer;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.SecurityContextAdapter;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;

import jakarta.annotation.PostConstruct;

/**
 * @author Andrei_Tytsik
 */
public class KafkaAuthorizer implements Authorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAuthorizer.class);

    @Autowired
    private SecurityContextAdapter securityContext;
    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private KafkaAuthorizerProperties authzProperties;
    @Autowired
    private KafkaManagerProperties properties;

    private ClusterMetadataAuthorizer authorizer;

    @PostConstruct
    private void init() {
        initAuthorizer();
    }

    private void initAuthorizer() {
        LOGGER.info("Authorizer start initializing...");
        try {
            authorizer = (ClusterMetadataAuthorizer) Class.forName(
                    authzProperties.getAuthorizerClass()).getDeclaredConstructor().newInstance();

            authorizer.configure(authzProperties.getAuthorizerConfig());

            if (authorizer instanceof Monitorable monitorable) {
                // configure with noop plugin metrics
                monitorable.withPluginMetrics(
                        new PluginMetricsImpl(new Metrics(), Collections.emptyMap())
                );
            }
            authorizer.completeInitialLoad();

            AuthorizerServerInfo serverInfo = buildAuthorizerServerInfo();
            authorizer.start(serverInfo).values().stream()
                    .map(CompletionStage::toCompletableFuture)
                    .forEach(CompletableFuture::join);

            LOGGER.info("Authorizer is initialized successfully...");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize authorizer", ex);
        }
    }

    @Override
    public boolean isPermitted(EntityType entityType, Object entityId, Operation operation) {
        Validate.notNull(entityType, "Entity type is null");
        Validate.notNull(operation, "Operation is null");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("checking KafkaAuthorizer.isPermitted for user={}, entityType={}, entityId={}, operation={}", securityContext.getIdentity(), entityType, entityId, operation);
        }

        if (isAdmin()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("User {} has ADMIN rights", securityContext.getIdentity());
            }
            return true;
        }

        StopWatch stopWatch = new StopWatch();
        if (LOGGER.isDebugEnabled()) {
            stopWatch.start();
        }
        try {
            boolean authorizationCheckResult = authorize(entityType, entityId, operation);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("authorizationCheckResult={} for user={}, entityType={}, entityId={}, operation={}", authorizationCheckResult, securityContext.getIdentity(), entityType, entityId, operation);
            }
            return authorizationCheckResult;
        } finally {
            if (LOGGER.isDebugEnabled()) {
                stopWatch.stop();
                LOGGER.debug("checking KafkaAuthorizer.isPermitted entityType='{}', entityId='{}', operation='{}' took {} ms",
                        entityType, entityId, operation, stopWatch.getTotalTimeMillis());
            }
        }

    }

    private boolean isAdmin() {
        if (authzProperties.getAdminRoles().isEmpty()) {
            return false;
        }
        Set<String> roles = securityContext.getRoles();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User roles: {}", roles);
        }
        return CollectionUtils.containsAny(roles, authzProperties.getAdminRoles());
    }

    private boolean authorize(EntityType entityType, Object entityId, Operation operation) {
        ResourcePattern resourcePattern = toResourcePattern(entityType, entityId);
        if (resourcePattern == null) {
            return false;
        }

        loadAclSnapshot();

        AuthorizableRequestContext requestContext = new SimpleAuthorizableRequestContext(
                getCurrentInetAddress(),
                getCurrentKafkaPrincipal());

        Action action = new Action(
                toAclOperation(operation),
                resourcePattern,
                1,
                true,
                true);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start authorization for user: {}: EntityType: {}, entityId: {}, operation: {}", securityContext.getIdentity(), entityType, entityId, operation);
        }
        return authorizer.
                authorize(requestContext, Collections.singletonList(action)).
                get(0) == AuthorizationResult.ALLOWED;
    }

    private void loadAclSnapshot() {
        StopWatch stopWatch = new StopWatch();
        if (LOGGER.isDebugEnabled()) {
            stopWatch.start();
            LOGGER.debug("Loading ACLs from Kafka...");
        }

        Collection<AclBinding> aclBindings = adminOperations.describeAcl(AclBindingFilter.ANY);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loaded {} ACL bindings from Kafka", aclBindings.size());
        }

        Map<Uuid, StandardAcl> aclsMap = new HashMap<>();
        for (AclBinding aclBinding : aclBindings) {
            StandardAcl standardAcl = StandardAcl.fromAclBinding(aclBinding);
            Uuid uuid = generateUniqueUuid(aclsMap.keySet());
            aclsMap.put(uuid, standardAcl);
        }

        authorizer.loadSnapshot(aclsMap);

        if (LOGGER.isDebugEnabled()) {
            stopWatch.stop();
            LOGGER.debug("ACLs loaded from Kafka in {} ms", stopWatch.getTotalTimeMillis());
        }
    }

    private Uuid generateUniqueUuid(Set<Uuid> existingUuids) {
        Uuid uuid;
        do {
            uuid = Uuid.randomUuid();
        } while (existingUuids.contains(uuid));
        return uuid;
    }

    private KafkaPrincipal getCurrentKafkaPrincipal() {
        return new KafkaPrincipal(
                KafkaPrincipal.USER_TYPE,
                securityContext.getIdentity());
    }

    private InetAddress getCurrentInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
    }

    private static AclOperation toAclOperation(Operation operation) {
        return switch (operation) {
            case READ -> AclOperation.READ;
            case WRITE -> AclOperation.WRITE;
            case CREATE -> AclOperation.CREATE;
            case DELETE -> AclOperation.DELETE;
            case ALTER -> AclOperation.ALTER;
            case ALTER_CONFIG -> AclOperation.ALTER_CONFIGS;
            case DESCRIBE -> AclOperation.DESCRIBE;
            default -> throw new IllegalArgumentException(
                    String.format("Operation '%s' not supported", operation));
        };
    }

    private static ResourcePattern toResourcePattern(EntityType entityType, Object entityId) {
        ResourceType resourceType = toResourceType(entityType);
        String resourceName = toResourceName(entityId);
        if (resourceType == null || resourceName == null) {
            return null;
        }

        return new ResourcePattern(resourceType, resourceName, PatternType.LITERAL);
    }

    private static ResourceType toResourceType(EntityType entityType) {
        return switch (entityType) {
            case CONSUMER_GROUP -> ResourceType.GROUP;
            case TOPIC -> ResourceType.TOPIC;
            case BROKER -> ResourceType.CLUSTER;
            default -> null;
        };
    }

    private static String toResourceName(Object entityId) {
        return Objects.toString(entityId, null);
    }

    /**
     * Generates "dummy" {@link AuthorizerServerInfo} with one early start listener
     */
    private static AuthorizerServerInfo buildAuthorizerServerInfo() {
        return new SimpleAuthorizerServerInfo(
                new ClusterResource("km-virtual-cluster"),
                1111,
                List.of(
                        new Endpoint("KM_EARLY_LISTENER", SecurityProtocol.SSL, "localhost", 1111),
                        new Endpoint("KM_SSL_LISTENER", SecurityProtocol.SSL, "localhost", 1112)
                ),
                new Endpoint("KM_INTER_LISTENER", SecurityProtocol.SSL, "localhost", 1113),
                List.of("KM_EARLY_LISTENER")
        );
    }

}
