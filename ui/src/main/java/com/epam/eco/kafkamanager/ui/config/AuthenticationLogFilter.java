/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static java.util.Objects.nonNull;

/**
 *  @author Mikhail_Vershkov
 */
public class AuthenticationLogFilter extends GenericFilterBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationLogFilter.class);

    @Value("${eco.kafkamanager.core.user-auth-logger:false}")
    private String loggerEnabled;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if( nonNull(authentication) && nonNull(loggerEnabled) && ("true".equalsIgnoreCase(loggerEnabled))) {
            if(authentication.getPrincipal() instanceof OidcUser oidcUser) {
                LOGGER.info("OIDC-USER: {} have been logged in. Attributes: {}", oidcUser.getName(), oidcUser.getAttributes() );
            } else {
                LOGGER.info("USER: {} have been logged", authentication.getName());
            }
        }

        chain.doFilter(request, response);
    }

}
