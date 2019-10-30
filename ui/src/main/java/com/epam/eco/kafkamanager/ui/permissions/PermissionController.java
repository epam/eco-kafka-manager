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
package com.epam.eco.kafkamanager.ui.permissions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionSearchQuery;
import com.epam.eco.kafkamanager.ui.permissions.export.PermissionExporterType;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class PermissionController {

    private static final int PAGE_SIZE = 10;

    public static final String VIEW = "permissions";
    public static final String ATTR_PAGE = "page";
    public static final String ATTR_SEARCH_QUERY = "searchQuery";
    public static final String ATTR_TOTAL_COUNT = "totalCount";

    public static final String MAPPING = "/permissions";
    public static final String MAPPING_EXPORT = MAPPING + "/export";

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String permissions(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        PermissionSearchQuery searchQuery = PermissionSearchQuery.fromJson(paramsMap);
        page = page != null && page > 0 ? page -1 : 0;

        Page<PermissionInfo> permissionPage = kafkaManager.getPermissionPage(
                searchQuery,
                PageRequest.of(page, PAGE_SIZE));

        model.addAttribute(ATTR_SEARCH_QUERY, searchQuery);
        model.addAttribute(ATTR_PAGE, wrap(permissionPage));
        model.addAttribute(ATTR_TOTAL_COUNT, kafkaManager.getPermissionCount());

        return VIEW;
    }

    @RequestMapping(value=MAPPING_EXPORT, method=RequestMethod.GET)
    public void export(
            @RequestParam PermissionExporterType exporterType,
            @RequestParam Map<String, Object> paramsMap,
            HttpServletResponse response) throws IOException {
        PermissionSearchQuery searchQuery = PermissionSearchQuery.fromJson(paramsMap);

        List<PermissionInfo> permissionInfos = kafkaManager.getPermissions(searchQuery);

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

    private Page<PermissionInfoWrapper> wrap(Page<PermissionInfo> page) {
        return page.map(PermissionInfoWrapper::wrap);
    }

}
