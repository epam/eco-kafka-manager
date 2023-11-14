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
package com.epam.eco.kafkamanager.rest.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * @author Andrei_Tytsik
 */
public final class HttpSecurityConfigurer {

    public static final String ADMIN_ROLE = "ECO-KM-ADMIN";

    public static HttpSecurity configureAll(HttpSecurity http) throws Exception {
        configureAuthorizeRequests(http);
        configureCsrf(http);
        return http;
    }

    public static HttpSecurity configureAuthorizeRequests(HttpSecurity http) throws Exception {
        return http.
                authorizeHttpRequests(request->request
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ADMIN_ROLE)
                        .anyRequest().authenticated());
    }

    public static HttpSecurity configureCsrf(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable);
    }

}
