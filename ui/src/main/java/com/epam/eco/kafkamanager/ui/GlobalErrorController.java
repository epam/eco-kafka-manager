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
package com.epam.eco.kafkamanager.ui;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class GlobalErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorController.class);

    public static final String MAPPING = "/error";
    public static final String VIEW = "error";

    public static final String ERROR_ATTRIBUTES = "errorAttributes";

    public static final String ATTR_PATH = "path";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping(value = MAPPING)
    public String error(WebRequest request, Model model) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(request, false);
        enrichErrorAttributes(request, attributes);
        model.addAttribute(ERROR_ATTRIBUTES, attributes);

        Throwable error = errorAttributes.getError(request);
        if (error != null) {
            LOGGER.error(String.format("Request '%s' failed", attributes.get(ATTR_PATH)), error);
        }

        return VIEW;
    }

    private void enrichErrorAttributes(WebRequest request, Map<String, Object> attributes) {
    }

}
