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
package com.epam.eco.kafkamanager.core.authz.kafka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import kafka.security.authorizer.AclAuthorizer;

/**
 * @author Andrei_Tytsik
 */
@ConfigurationProperties(prefix = "eco.kafkamanager.core.authz.kafka")
public class KafkaAuthorizerProperties {

    private boolean enabled = false;
    private Set<String> adminRoles = new HashSet<>();
    private String authorizerClass = AclAuthorizer.class.getName();
    private Map<String, Object> authorizerConfig = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public Set<String> getAdminRoles() {
        return adminRoles;
    }
    public void setAdminRoles(Set<String> adminRoles) {
        this.adminRoles = adminRoles;
    }
    public String getAuthorizerClass() {
        return authorizerClass;
    }
    public void setAuthorizerClass(String authorizerClass) {
        this.authorizerClass = authorizerClass;
    }
    public Map<String, Object> getAuthorizerConfig() {
        return authorizerConfig;
    }
    public void setAuthorizerConfig(Map<String, Object> authorizerConfig) {
        this.authorizerConfig = authorizerConfig;
    }

    @Override
    public String toString() {
        return
                "{enabled: " + enabled +
                ", adminRoles: " + adminRoles +
                ", authorizerClass: " + authorizerClass +
                ", authorizerConfig: " + authorizerConfig +
                "}";
    }

}
