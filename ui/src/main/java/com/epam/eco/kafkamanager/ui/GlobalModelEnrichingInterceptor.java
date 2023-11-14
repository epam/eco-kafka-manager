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
package com.epam.eco.kafkamanager.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
public class GlobalModelEnrichingInterceptor implements HandlerInterceptor {

    public static final String ATTR_BUILD_INFO = "buildInfo";
    public static final String ATTR_PRINCIPAL = "principal";
    public static final String ATTR_PREFERRED_USERNAME = "preferred_username";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_GA_TRACKING_ID = "gaTrackingId";

    @Autowired
    private KafkaManagerUiProperties properties;

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {

        if (
                modelAndView == null || // ignore ajax requests
                isRedirectView(modelAndView) // ignore redirect views
                ) {
            return;
        }

        modelAndView.addObject(ATTR_BUILD_INFO, BuildInfo.instance());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(nonNull(SecurityContextHolder.getContext().getAuthentication())) {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                modelAndView.addObject(
                        ATTR_PRINCIPAL,
                        isNull(oAuth2User.getAttribute(ATTR_PREFERRED_USERNAME)) ?
                             oAuth2User.getAttribute(ATTR_NAME) : oAuth2User.getAttribute(ATTR_PREFERRED_USERNAME));
            } else {
                modelAndView.addObject(
                        ATTR_PRINCIPAL,
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            }
        }

        modelAndView.addObject(ATTR_GA_TRACKING_ID, properties.getGaTrackingId());
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        return
                modelAndView.getView() instanceof RedirectView ||
                StringUtils.startsWith(modelAndView.getViewName(), "redirect:");
    }

}
