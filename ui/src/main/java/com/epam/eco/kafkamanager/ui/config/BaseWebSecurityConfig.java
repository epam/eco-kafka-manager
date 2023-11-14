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

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.Collections;

/**
 * @author Mikhail_Vershkov
 */
public class BaseWebSecurityConfig {

    public static final String ADMIN_ROLE = "ECO-KM-ADMIN";

    // Workaround for the correct filling userInfo structure
    // https://github.com/spring-projects/spring-security/issues/12144
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService oidcUserService = new OidcUserService();
        oidcUserService.setAccessibleScopes(Collections.emptySet());
        return oidcUserService;
    }
    public HttpSecurity commonHttpConfig(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests(requests ->
                        requests.requestMatchers("/**/*.css",
                                        "/**/*.js",
                                        "/**/*.woff2",
                                        "/**/*.woff",
                                        "/**/*.ttf",
                                        "/**/*.svg",
                                        "/**/*.png",
                                        "/styles/fonts/**",
                                        "/logout",
                                        "/actuator/health",
                                        "/actuator/info",
                                        "/actuator/metrics",
                                        "/actuator/prometheus",
                                        "/login/**").permitAll().
                                requestMatchers(HttpMethod.POST, "/udmetrics/topic_offset_increase/*").hasRole(ADMIN_ROLE).
                                requestMatchers(HttpMethod.DELETE, "/udmetrics/topic_offset_increase/*").hasRole(ADMIN_ROLE).
                                requestMatchers(HttpMethod.POST, "/udmetrics/consumer_group_lag/*").hasRole(ADMIN_ROLE).
                                requestMatchers(HttpMethod.DELETE, "/udmetrics/consumer_group_lag/*").hasRole(ADMIN_ROLE).
                                requestMatchers("/metadata*").hasRole(ADMIN_ROLE).
                                requestMatchers("/metadata_save").hasRole(ADMIN_ROLE).
                                requestMatchers("/metadata_delete").hasRole(ADMIN_ROLE).
                                requestMatchers("/permissions/export*").hasRole(ADMIN_ROLE).
                                requestMatchers("/topics_export*").hasRole(ADMIN_ROLE).
                                anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler()));

    }

    // Workaround for fixing a problem with X-CSRF-TOKEN
    // in Spring Security 6.0 it refreshed only when secure request (POST,DELETE,PUT,PATCH) have been requested,
    // instead of previous versions, where every http method was caused token refresh
    // this is the reason that we have to do this:
    // requestHandler.setCsrfRequestAttributeName(null);
    public CsrfTokenRequestAttributeHandler requestHandler() {
        CsrfTokenRequestAttributeHandler requestHandler= new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        return requestHandler;
    }

}
