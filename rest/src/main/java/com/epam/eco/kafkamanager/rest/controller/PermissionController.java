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
package com.epam.eco.kafkamanager.rest.controller;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PermissionCreateParams;
import com.epam.eco.kafkamanager.PermissionDeleteParams;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.core.utils.PageUtils;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;
import com.epam.eco.kafkamanager.rest.request.PermissionRequest;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private KafkaManager kafkaManager;

    @GetMapping
    public Page<PermissionInfo> getPermissionPage(
            @RequestParam(value = "kafkaPrincipal", required = false) String kafkaPrincipal,
            @RequestParam(value = "resourceType", required = false) ResourceType resourceType,
            @RequestParam(value = "resourceName", required = false) String resourceName,
            @RequestParam(value = "permissionType", required = false) AclPermissionType permissionType,
            @RequestParam(value = "operation", required = false) AclOperation operation,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = PageUtils.buildPageableWithDefaultsIfNull(page, pageSize);
        PermissionSearchCriteria criteria = PermissionSearchCriteria.builder()
                .kafkaPrincipal(kafkaPrincipal)
                .resourceType(resourceType)
                .resourceName(resourceName)
                .permissionType(permissionType)
                .operation(operation)
                .host(host)
                .description(description)
                .build();

        return kafkaManager.getPermissionPage(criteria, pageable);
    }

    @PostMapping
    public void postPermission(@RequestBody PermissionRequest request) {
        PermissionCreateParams params = PermissionCreateParams.builder()
                .resourceType(request.getResourceType())
                .resourceName(request.getResourceName())
                .principal(request.getPrincipal())
                .permissionType(request.getPermissionType())
                .operation(request.getOperation())
                .host(request.getHost())
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        kafkaManager.createPermission(params);
    }

    @DeleteMapping("/{resourceType}/{resourceName}/{principal}/{permissionType}/{operation}/{host}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("principal") String principal,
            @PathVariable("permissionType") AclPermissionType permissionType,
            @PathVariable("operation") AclOperation operation,
            @PathVariable("host") String host) {
        PermissionDeleteParams params = PermissionDeleteParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .principal(principal)
                .permissionType(permissionType)
                .operation(operation)
                .host(host)
                .build();
        kafkaManager.deletePermission(params);
    }

    @PutMapping("/{resourceType}/{resourceName}/{principal}/metadata")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putPermissionMetadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("principal") String principal,
            @RequestBody MetadataRequest request) {
        PermissionMetadataUpdateParams params = PermissionMetadataUpdateParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .principal(principal)
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        kafkaManager.updatePermission(params);
    }

    @DeleteMapping("/{resourceType}/{resourceName}/{principal}/metadata")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermissionMetadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("principal") String principal) {
        PermissionMetadataDeleteParams params = PermissionMetadataDeleteParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .principal(principal)
                .build();
        kafkaManager.updatePermission(params);
    }

}
