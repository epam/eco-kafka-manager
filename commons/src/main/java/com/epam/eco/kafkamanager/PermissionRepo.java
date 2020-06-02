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
package com.epam.eco.kafkamanager;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;

import com.epam.eco.kafkamanager.repo.ValueRepo;

/**
 * @author Andrei_Tytsik
 */
public interface PermissionRepo extends ValueRepo<PermissionInfo, PermissionSearchCriteria> {

    @PreAuthorize("@authorizer.isPermitted('PERMISSION', null, 'WRITE')")
    void create(ResourceType resourceType,
                String resourceName,
                KafkaPrincipal principal,
                AclPermissionType permissionType,
                AclOperation operation,
                String host);

    @PreAuthorize("@authorizer.isPermitted('PERMISSION', null, 'WRITE')")
    void delete(ResourceType resourceType,
                String resourceName,
                KafkaPrincipal principal,
                AclPermissionType permissionType,
                AclOperation operation,
                String host);

}
