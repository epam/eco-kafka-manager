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
package com.epam.eco.kafkamanager.ui.metrics.reporter.hawkular;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Andrei_Tytsik
 */
@ConfigurationProperties(prefix = "eco.kafkamanager.ui.metrics.reporter.hawkular")
public class HawkularReporterProperties {

    private boolean enabled = false;
    private String tenant = "test-kafkamanager";
    private String url = "localhost";
    private long pollInvervalInMs = 60 * 1000;
    private String username;
    private String password;

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getTenant() {
        return tenant;
    }
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public long getPollInvervalInMs() {
        return pollInvervalInMs;
    }
    public void setPollInvervalInMs(long pollInvervalInMs) {
        this.pollInvervalInMs = pollInvervalInMs;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
