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

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrei_Tytsik
 */
public class BuildInfo {

    private static final BuildInfo INSTANCE = new BuildInfo();

    private static final String PATH = "/buildinfo.properties";
    private static final String BUILD_DATE = "build.date";
    private static final String BUILD_VERSION = "build.version";

    private final Properties properties = new Properties();

    private BuildInfo() {
        try (InputStream buildInfoInputStream = this.getClass().getResourceAsStream(PATH)) {
            properties.load(buildInfoInputStream);
            buildDate = readProperty(BUILD_DATE, "N/A");
            buildVersion = readProperty(BUILD_VERSION, "N/A");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read build info properties", ex);
        }
    }

    private String buildVersion;
    private String buildDate;

    public static BuildInfo instance() {
        return INSTANCE;
    }

    public String getBuildVersion() {
        return buildVersion;
    }
    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }
    public String getBuildDate() {
        return buildDate;
    }
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    private String readProperty(String key, String defaultValue) {
        String property = properties.getProperty(key);
        return !StringUtils.isBlank(property) ? property : defaultValue;
    }

}
