/*
 * Copyright 2019 EPAM Systems
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
package com.epam.eco.kafkamanager.rest.controller;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.RecordFetchRequest;
import com.epam.eco.kafkamanager.TopicSearchQuery;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/storage-type")
    public ConsumerGroupInfo.StorageType[] storageTypes() {
        return ConsumerGroupInfo.StorageType.values();
    }

    @GetMapping("/acl-permission-type")
    public AclPermissionType[] aclPermissionTypes() {
        return AclPermissionType.values();
    }

    @GetMapping("/acl-operation")
    public AclOperation[] aclOperations() {
        return AclOperation.values();
    }

    @GetMapping("/resource-type")
    public ResourceType[] resourceTypes() {
        return ResourceType.values();
    }

    @GetMapping("/replication-state")
    public TopicSearchQuery.ReplicationState[] replicationStates() {
        return TopicSearchQuery.ReplicationState.values();
    }

    @GetMapping("/data-format")
    public RecordFetchRequest.DataFormat[] dataFormats() {
        return RecordFetchRequest.DataFormat.values();
    }

    @GetMapping("/security-protocol")
    public SecurityProtocol[] securityProtocols() {
        return SecurityProtocol.values();
    }

    @GetMapping("/transaction-state")
    public TransactionState[] transactionState() {
        return TransactionState.values();
    }

}
