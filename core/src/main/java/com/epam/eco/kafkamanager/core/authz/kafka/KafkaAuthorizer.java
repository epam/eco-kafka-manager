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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.server.authorizer.Action;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.authorizer.AuthorizationResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.Authorizer;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.SecurityContextAdapter;

import kafka.server.KafkaConfig;

/**
 * @author Andrei_Tytsik
 */
public class KafkaAuthorizer implements Authorizer {

    @Autowired
    private SecurityContextAdapter securityContext;
    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private KafkaAuthorizerProperties authzProperties;

    private org.apache.kafka.server.authorizer.Authorizer authorizer;

    @PostConstruct
    private void init() {
        initAuthorizer();
    }

    private void initAuthorizer() {
        try {
            authorizer = (org.apache.kafka.server.authorizer.Authorizer)Class.forName(
                    authzProperties.getAuthorizerClass()).newInstance();

            Map<String, Object> authorizerConfig =
                    new HashMap<>(authzProperties.getAuthorizerConfig());
            authorizerConfig.put(
                    KafkaConfig.ZkConnectProp(),
                    adminOperations.getZkConnect());

            authorizer.configure(authorizerConfig);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            throw new IllegalArgumentException("Failed to initialize authorizer", ex);
        }
    }

    @Override
    public boolean isPermitted(EntityType entityType, Object entityId, Operation operation) {
        Validate.notNull(entityType, "Entity type is null");
        Validate.notNull(operation, "Operation is null");

        if (isAdmin()) {
            return true;
        }

        return authorize(entityType, entityId, operation);
    }

    private boolean isAdmin() {
        if (authzProperties.getAdminRoles().isEmpty()) {
            return false;
        }

        Set<String> roles = securityContext.getRoles();
        return CollectionUtils.containsAny(roles, authzProperties.getAdminRoles());
    }

    private boolean authorize(EntityType entityType, Object entityId, Operation operation) {
        ResourcePattern resourcePattern = toResourcePattern(entityType, entityId);
        if (resourcePattern == null) {
            return false;
        }

        AuthorizableRequestContext requestContext = new SimpleAuthorizableRequestContext(
                getCurrentInetAddress(),
                getCurrentKafkaPrincipal());

        Action action = new Action(
                toAclOperation(operation),
                resourcePattern,
                1,
                true,
                true);

        return authorizer.
                authorize(requestContext, Collections.singletonList(action)).
                get(0) == AuthorizationResult.ALLOWED;
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
        switch (operation) {
        case READ: return AclOperation.READ;
        case WRITE: return AclOperation.WRITE;
        case CREATE: return AclOperation.CREATE;
        case DELETE: return AclOperation.DELETE;
        case ALTER: return AclOperation.ALTER;
        case ALTER_CONFIG: return AclOperation.ALTER_CONFIGS;
        case DESCRIBE: return AclOperation.DESCRIBE;
        default: throw new IllegalArgumentException(
                String.format("Operation '%s' not supported", operation));
        }
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
        switch (entityType) {
        case CONSUMER_GROUP: return ResourceType.GROUP;
        case TOPIC: return ResourceType.TOPIC;
        case BROKER: return ResourceType.CLUSTER;
        default: return null;
        }
    }

    private static String toResourceName(Object entityId) {
        return Objects.toString(entityId, null);
    }

}
