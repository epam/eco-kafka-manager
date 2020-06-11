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
package com.epam.eco.kafkamanager.core.authz.kafka;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.Authorizer;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.SecurityContextAdapter;

import kafka.network.RequestChannel.Session;
import kafka.security.auth.Resource;
import kafka.security.auth.ResourceType;
import kafka.security.auth.ResourceType$;
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

    private kafka.security.auth.Authorizer authorizer;

    @PostConstruct
    private void init() {
        initAuthorizer();
    }

    private void initAuthorizer() {
        try {
            authorizer =
                    (kafka.security.auth.Authorizer)Class.forName(
                            authzProperties.getAuthorizerClass()).newInstance();

            Map<String, Object> authorizerConfig =
                    new HashMap<>(authzProperties.getAuthorizerConfig());
            authorizerConfig.put(
                    KafkaConfig.ZkConnectProp(),
                    adminOperations.getZkConnect());

            authorizer.configure(authorizerConfig);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            throw new RuntimeException("Failed to initialize authorizer", ex);
        }
    }

    @Override
    public boolean isPermitted(EntityType entityType, Object entityId, Operation operation) {
        Validate.notNull(entityType, "Entity type is null");
        Validate.notNull(operation, "Operation is null");

        if (isAdmin()) {
            return true;
        }

        Resource resource = toResource(entityType, entityId);
        if (resource == null) {
            return false;
        }

        return authorizer.authorize(
                createSession(),
                toScalaOperation(operation),
                resource);
    }

    private boolean isAdmin() {
        if (authzProperties.getAdminRoles().isEmpty()) {
            return false;
        }

        Set<String> roles = securityContext.getRoles();
        return CollectionUtils.containsAny(roles, authzProperties.getAdminRoles());
    }

    private Session createSession() {
        KafkaPrincipal kafkaPrincipal = new KafkaPrincipal(
                KafkaPrincipal.USER_TYPE,
                securityContext.getIdentity());
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
        return new Session(kafkaPrincipal, inetAddress);
    }

    private static kafka.security.auth.Operation toScalaOperation(Operation operation) {
        if (Operation.READ == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.READ);
        } else if (Operation.WRITE == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.WRITE);
        } else if (Operation.CREATE == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.CREATE);
        } else if (Operation.DELETE == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.DELETE);
        } else if (Operation.ALTER == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.ALTER);
        } else if (Operation.ALTER_CONFIG == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.ALTER_CONFIGS);
        } else if (Operation.DESCRIBE == operation) {
            return kafka.security.auth.Operation$.MODULE$.fromJava(AclOperation.DESCRIBE);
        } else {
            throw new IllegalArgumentException(
                    String.format("Operation '%s' not supported", operation));
        }
    }

    private static Resource toResource(EntityType entityType, Object entityId) {
        ResourceType resourceType = toResourceType(entityType);
        String resourceName = toResourceName(entityId);
        if (resourceType == null || resourceName == null) {
            return null;
        }

        return Resource.apply(resourceType, resourceName, PatternType.LITERAL);
    }

    private static ResourceType toResourceType(EntityType entityType) {
        if (EntityType.CONSUMER_GROUP == entityType) {
            return ResourceType$.MODULE$.fromJava(
                    org.apache.kafka.common.resource.ResourceType.GROUP);
        } else if (EntityType.TOPIC == entityType) {
            return ResourceType$.MODULE$.fromJava(
                    org.apache.kafka.common.resource.ResourceType.TOPIC);
        } else if (EntityType.BROKER == entityType) {
            return ResourceType$.MODULE$.fromJava(
                    org.apache.kafka.common.resource.ResourceType.CLUSTER);
        } else {
            return null;
        }
    }

    private static String toResourceName(Object entityId) {
        return Objects.toString(entityId, null);
    }

}
