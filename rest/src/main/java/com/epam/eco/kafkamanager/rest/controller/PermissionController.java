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
package com.epam.eco.kafkamanager.rest.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
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
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.PrincipalPermissionsDeleteParams;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.ResourcePermissionsDeleteParams;
import com.epam.eco.kafkamanager.core.utils.PageUtils;
import com.epam.eco.kafkamanager.export.PermissionExporterType;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;
import com.epam.eco.kafkamanager.rest.request.PermissionRequest;

import jakarta.servlet.http.HttpServletResponse;

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
            @RequestParam(value = "patternType", required = false) PatternType patternType,
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
                .patternType(patternType)
                .permissionType(permissionType)
                .operation(operation)
                .host(host)
                .description(description)
                .build();

        return kafkaManager.getPermissionPage(criteria, pageable);
    }

    @GetMapping("export")
    public void exportPermissions(
            @RequestParam(value = "exporterType", defaultValue = "CSV") PermissionExporterType exporterType,
            @RequestParam(value = "kafkaPrincipal", required = false) String kafkaPrincipal,
            @RequestParam(value = "resourceType", required = false) ResourceType resourceType,
            @RequestParam(value = "resourceName", required = false) String resourceName,
            @RequestParam(value = "patternType", required = false) PatternType patternType,
            @RequestParam(value = "permissionType", required = false) AclPermissionType permissionType,
            @RequestParam(value = "operation", required = false) AclOperation operation,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "description", required = false) String description,
            HttpServletResponse response) throws IOException {
        PermissionSearchCriteria criteria = PermissionSearchCriteria.builder()
                                                                    .kafkaPrincipal(kafkaPrincipal)
                                                                    .resourceType(resourceType)
                                                                    .resourceName(resourceName)
                                                                    .patternType(patternType)
                                                                    .permissionType(permissionType)
                                                                    .operation(operation)
                                                                    .host(host)
                                                                    .description(description)
                                                                    .build();
        List<PermissionInfo> permissions = kafkaManager.getPermissions(criteria);
        response.setContentType(exporterType.contentType());
        response.setHeader(
                "Content-Disposition",
                String.format(
                        "attachment; filename=\"%s_%d.txt\"",
                        exporterType.name(), System.currentTimeMillis()));

        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            exporterType.exporter().export(permissions, out);
            out.flush();
        }
    }

    @PostMapping
    public void postPermission(@RequestBody PermissionRequest request) {
        PermissionCreateParams params = PermissionCreateParams.builder()
                .resourceType(request.getResourceType())
                .resourceName(request.getResourceName())
                .patternType(request.getPatternType())
                .principal(request.getPrincipal())
                .permissionType(request.getPermissionType())
                .operation(request.getOperation())
                .host(request.getHost())
                .build();
        kafkaManager.createPermission(params);
    }

    @GetMapping("/resource/{resourceType}/{resourceName}/{patternType}")
    public List<PermissionInfo> getResourcePermissions(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam(value = "principalFilter", required = false) String principalFilter,
            @RequestParam(value = "permissionTypeFilter", required = false) AclPermissionType permissionTypeFilter,
            @RequestParam(value = "operationFilter", required = false) AclOperation operationFilter,
            @RequestParam(value = "hostFilter", required = false) String hostFilter) {
        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .principalFilter(principalFilter)
                .permissionTypeFilter(permissionTypeFilter)
                .operationFilter(operationFilter)
                .hostFilter(hostFilter)
                .build();

        return kafkaManager.getPermissionsOfResource(filter);
    }

    @DeleteMapping("/resource/{resourceType}/{resourceName}/{patternType}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourcePermissions(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam(value = "principalFilter", required = false) String principalFilter,
            @RequestParam(value = "permissionTypeFilter", required = false) AclPermissionType permissionTypeFilter,
            @RequestParam(value = "operationFilter", required = false) AclOperation operationFilter,
            @RequestParam(value = "hostFilter", required = false) String hostFilter) {
        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .principalFilter(principalFilter)
                .permissionTypeFilter(permissionTypeFilter)
                .operationFilter(operationFilter)
                .hostFilter(hostFilter)
                .build();

        kafkaManager.deletePermissions(new ResourcePermissionsDeleteParams(filter));
    }

    @DeleteMapping("/principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrincipalPermissions(
            @RequestParam("principalFilter") String principal) {
        PrincipalPermissionsDeleteParams params = PrincipalPermissionsDeleteParams.builder()
                .principal(principal)
                .build();
        kafkaManager.deletePermissions(params);
    }

    @PutMapping("/resource/{resourceType}/{resourceName}/{patternType}/metadata")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putPermissionMetadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("principal") String principal,
            @RequestBody MetadataRequest request) {
        PermissionMetadataUpdateParams params = PermissionMetadataUpdateParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .principal(principal)
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        kafkaManager.updatePermission(params);
    }

    @DeleteMapping("/resource/{resourceType}/{resourceName}/{patternType}/metadata")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermissionMetadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("principal") String principal) {
        PermissionMetadataDeleteParams params = PermissionMetadataDeleteParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .principal(principal)
                .build();
        kafkaManager.updatePermission(params);
    }

}
