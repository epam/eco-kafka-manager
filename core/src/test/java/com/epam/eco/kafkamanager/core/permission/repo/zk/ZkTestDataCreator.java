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
package com.epam.eco.kafkamanager.core.permission.repo.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;

import com.epam.eco.commons.kafka.AdminClientUtils;
import com.epam.eco.commons.kafka.config.AdminClientConfigBuilder;

/**
 * @author Andrei_Tytsik
 */
public class ZkTestDataCreator {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String[] TOPICS = {"topic1", "topic2", "topic3"};
    private static final String PRINCIPAL = "User:test-principal";
    private static final String HOST = "*";
    private static final AclOperation[] OPERATIONS = {
            AclOperation.READ,
            AclOperation.WRITE,
            AclOperation.DELETE,
            AclOperation.DESCRIBE,
            AclOperation.ALTER};

    public static void main(String[] args) throws Exception {
        cteate();
        delete();
    }

    private static void cteate() throws Exception {
        List<AclBinding> bindings = new ArrayList<>();
        for (String topic : TOPICS) {
            org.apache.kafka.common.resource.Resource resource =
                    new org.apache.kafka.common.resource.Resource(ResourceType.TOPIC, topic);
            for (AclOperation operation : OPERATIONS) {
                AccessControlEntry entry = new AccessControlEntry(
                        PRINCIPAL,
                        HOST,
                        operation,
                        AclPermissionType.ALLOW);
                bindings.add(
                        new AclBinding(
                                new ResourcePattern(resource.resourceType(), resource.name(), PatternType.LITERAL),
                                entry));
            }
        }

        List<Callable<Void>> tasks = new ArrayList<>();
        for (AclBinding binding : bindings) {
            tasks.add(() -> {
                try {
                    AdminClientUtils.createAcl(
                            AdminClientConfigBuilder.
                                withEmpty().
                                bootstrapServers(BOOTSTRAP_SERVERS).
                                build(), binding);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        ExecutorService es = Executors.newFixedThreadPool(30);
        es.invokeAll(tasks);
        es.shutdown();
        es.awaitTermination(60, TimeUnit.SECONDS);
    }

    private static void delete() throws Exception {
        List<AclBindingFilter> bindings = new ArrayList<>();
        for (String topic : TOPICS) {
            for (AclOperation operation : OPERATIONS) {
                AccessControlEntryFilter entry = new AccessControlEntryFilter(
                        PRINCIPAL,
                        HOST,
                        operation,
                        AclPermissionType.ALLOW);
                bindings.add(
                        new AclBindingFilter(
                                new ResourcePatternFilter(
                                        ResourceType.TOPIC,
                                        topic,
                                        PatternType.LITERAL),
                                entry));
            }
        }

        List<Callable<Void>> tasks = new ArrayList<>();
        for (AclBindingFilter binding : bindings) {
            tasks.add(() -> {
                try {
                    AdminClientUtils.deleteAcl(
                            AdminClientConfigBuilder.
                                withEmpty().
                                bootstrapServers(BOOTSTRAP_SERVERS).
                                build(), binding);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        ExecutorService es = Executors.newFixedThreadPool(30);
        es.invokeAll(tasks);
        es.shutdown();
        es.awaitTermination(60, TimeUnit.SECONDS);
    }

}
