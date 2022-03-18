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
package com.epam.eco.kafkamanager.ui.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author Andrei_Tytsik
 */
public final class HttpSecurityConfigurer {

    public static final String ADMIN_ROLE = "ECO-KM-ADMIN";

    public static HttpSecurity configureAll(HttpSecurity http) throws Exception {
        configureAuthorizeRequests(http);
        configureLogout(http);
        return http;
    }

    public static HttpSecurity configureAuthorizeRequests(HttpSecurity http) throws Exception {
        return http.authorizeRequests().
                antMatchers(
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.woff2",
                        "/**/*.woff",
                        "/**/*.ttf",
                        "/logout",
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/metrics",
                        "/actuator/prometheus").permitAll().
                antMatchers(HttpMethod.POST, "/udmetrics/topic_offset_increase/*").hasRole(ADMIN_ROLE).
                antMatchers(HttpMethod.DELETE, "/udmetrics/topic_offset_increase/*").hasRole(ADMIN_ROLE).
                antMatchers(HttpMethod.POST, "/udmetrics/consumer_group_lag/*").hasRole(ADMIN_ROLE).
                antMatchers(HttpMethod.DELETE, "/udmetrics/consumer_group_lag/*").hasRole(ADMIN_ROLE).
                antMatchers("/metadata*").hasRole(ADMIN_ROLE).
                antMatchers("/metadata_save").hasRole(ADMIN_ROLE).
                antMatchers("/metadata_delete").hasRole(ADMIN_ROLE).
                antMatchers("/permissions/export*").hasRole(ADMIN_ROLE).
                antMatchers("/topics_export*").hasRole(ADMIN_ROLE).
                anyRequest().authenticated().
                and();
    }

    public static HttpSecurity configureLogout(HttpSecurity http) throws Exception {
        return http.logout().
                logoutRequestMatcher(new AntPathRequestMatcher("/logout")).
                and();
    }

}
