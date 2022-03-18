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
package com.epam.eco.kafkamanager.ui.permissions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PermissionCreateParams;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.ui.permissions.export.PermissionExporterType;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class PermissionController {

    private static final int PAGE_SIZE = 10;

    public static final String PERMISSIONS_VIEW = "permissions";
    public static final String PERMISSION_CREATE_VIEW = "permission_create";

    public static final String ATTR_PAGE = "page";
    public static final String ATTR_SEARCH_CRITERIA = "searchCriteria";
    public static final String ATTR_TOTAL_COUNT = "totalCount";
    public static final String ATTR_DEFAULT_RESOURCE_TYPE = "defaultResourceType";
    public static final String ATTR_DEFAULT_RESOURCE_NAME = "defaultResourceName";
    public static final String ATTR_DEFAULT_PATTERN_TYPE = "defaultPatternType";
    public static final String ATTR_DEFAULT_KAFKA_PRINCIPAL = "defaultKafkaPrincipal";
    public static final String ATTR_DEFAULT_PERMISSION_TYPE = "defaultPermissionType";
    public static final String ATTR_DEFAULT_OPERATION = "defaultOperation";
    public static final String ATTR_DEFAULT_HOST = "defaultHost";

    public static final String MAPPING_PERMISSIONS = "/permissions";
    public static final String MAPPING_EXPORT = MAPPING_PERMISSIONS + "/export";
    public static final String MAPPING_CREATE = MAPPING_PERMISSIONS + "/create";

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value = MAPPING_PERMISSIONS, method = RequestMethod.GET)
    public String permissions(
            @RequestParam(required = false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        PermissionSearchCriteria searchCriteria = PermissionSearchCriteria.fromJson(paramsMap);
        page = page != null && page > 0 ? page -1 : 0;

        Page<PermissionInfo> permissionPage = kafkaManager.getPermissionPage(
                searchCriteria,
                PageRequest.of(page, PAGE_SIZE));

        model.addAttribute(ATTR_SEARCH_CRITERIA, searchCriteria);
        model.addAttribute(ATTR_PAGE, wrap(permissionPage));
        model.addAttribute(ATTR_TOTAL_COUNT, kafkaManager.getPermissionCount());

        return PERMISSIONS_VIEW;
    }

    @RequestMapping(value = MAPPING_EXPORT, method = RequestMethod.GET)
    public void export(
            @RequestParam PermissionExporterType exporterType,
            @RequestParam Map<String, Object> paramsMap,
            HttpServletResponse response) throws IOException {
        PermissionSearchCriteria searchCriteria = PermissionSearchCriteria.fromJson(paramsMap);

        List<PermissionInfo> permissionInfos = kafkaManager.getPermissions(searchCriteria);

        response.setContentType(exporterType.contentType());
        response.setHeader(
                "Content-Disposition",
                String.format(
                        "attachment; filename=\"%s_%d.txt\"",
                        exporterType.name(), System.currentTimeMillis()));

        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            exporterType.exporter().export(permissionInfos, out);
            out.flush();
        }
    }

    @RequestMapping(value = MAPPING_CREATE, method = RequestMethod.GET)
    public String create(
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) PatternType patternType,
            @RequestParam(required = false) AclPermissionType permissionType,
            @RequestParam(required = false) AclOperation operation,
            @RequestParam(name = "kafkaPrincipal", required = false) String kafkaPrincipalString,
            @RequestParam(required = false) String host,
            Model model) {
        resourceType = resourceType != null ? resourceType : ResourceType.TOPIC;
        patternType = patternType != null ? patternType : PatternType.LITERAL;
        permissionType = permissionType != null ? permissionType : AclPermissionType.ALLOW;
        operation = operation != null ? operation : AclOperation.DESCRIBE;
        kafkaPrincipalString =
                !StringUtils.isBlank(kafkaPrincipalString) ? kafkaPrincipalString : KafkaPrincipal.USER_TYPE + ":";
        host =
                !StringUtils.isBlank(host) ? host : "*";

        model.addAttribute(ATTR_DEFAULT_RESOURCE_TYPE, resourceType);
        model.addAttribute(ATTR_DEFAULT_RESOURCE_NAME, resourceName);
        model.addAttribute(ATTR_DEFAULT_PATTERN_TYPE, patternType);
        model.addAttribute(ATTR_DEFAULT_KAFKA_PRINCIPAL, kafkaPrincipalString);
        model.addAttribute(ATTR_DEFAULT_PERMISSION_TYPE, permissionType);
        model.addAttribute(ATTR_DEFAULT_OPERATION, operation);
        model.addAttribute(ATTR_DEFAULT_HOST, host);

        return PERMISSION_CREATE_VIEW;
    }

    @RequestMapping(value = MAPPING_CREATE, method = RequestMethod.POST)
    public String create(
            @RequestParam ResourceType resourceType,
            @RequestParam String resourceName,
            @RequestParam PatternType patternType,
            @RequestParam AclPermissionType permissionType,
            @RequestParam AclOperation operation,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            @RequestParam String host) {
        PermissionCreateParams.Builder createParamsBuilder = PermissionCreateParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .permissionType(permissionType)
                .operation(operation)
                .principal(kafkaPrincipalString)
                .host(host);

        kafkaManager.createPermission(createParamsBuilder.build());
        return "redirect:" + ResourcePermissionController.buildResourceUrl(
                resourceType,
                resourceName,
                patternType,
                kafkaPrincipalString);
    }

    private Page<PermissionInfoWrapper> wrap(Page<PermissionInfo> page) {
        return page.map(PermissionInfoWrapper::wrap);
    }

}
