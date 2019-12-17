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
package com.epam.eco.kafkamanager.ui.common;

import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.ConfigKey;

import com.epam.eco.commons.kafka.config.BrokerConfigDef;
import com.epam.eco.commons.kafka.config.TopicConfigDef;

/**
 * @author Andrei_Tytsik
 */
public class ConfigEntryWrapper {

    private final ConfigEntry entry;
    private final ConfigKey def;

    public ConfigEntryWrapper(ConfigEntry entry, ConfigKey def) {
        Validate.notNull(entry, "Entry is null");
        Validate.notNull(def, "Definition is null");

        this.entry = entry;
        this.def = def;
    }

    public static ConfigEntryWrapper wrap(ConfigEntry entry, ConfigKey key) {
        return new ConfigEntryWrapper(entry, key);
    }

    public static ConfigEntryWrapper wrapForTopic(ConfigEntry entry) {
        Validate.notNull(entry, "Entry is null");

        return new ConfigEntryWrapper(
                entry,
                TopicConfigDef.INSTANCE.key(entry.name()));
    }

    public static ConfigEntryWrapper wrapForBroker(ConfigEntry entry) {
        Validate.notNull(entry, "Entry is null");

        ConfigKey key = BrokerConfigDef.INSTANCE.key(entry.name());
        return new ConfigEntryWrapper(entry, key == null ? defaultConfigKey(entry.name()) : key);
    }

    private static ConfigKey defaultConfigKey(String name) {
        Validate.notBlank(name, "Name can't be blank");

        return new ConfigKey(
                name, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, null, ConfigDef.Importance.MEDIUM,
                "N/A", null, 0,  ConfigDef.Width.MEDIUM, name, Collections.emptyList(),
                null, false);
    }

    public String getName() {
        return entry.name();
    }

    public String getValue() {
        return entry.value();
    }

    public String getValueOrNullIfDefault() {
        return !isDefault() ? getValue() : null;
    }

    public String getValueOrMaskIfSensitive() {
        return !isSensitive() ? getValue() : "**********";
    }

    public boolean isDefault() {
        return entry.isDefault();
    }

    public boolean isSensitive() {
        return entry.isSensitive();
    }

    public boolean isReadOnly() {
        return entry.isReadOnly();
    }

    public ConfigKey getDef() {
        return def;
    }

}
