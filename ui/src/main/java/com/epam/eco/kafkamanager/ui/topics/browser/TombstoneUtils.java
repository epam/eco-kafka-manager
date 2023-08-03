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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.epam.eco.kafkamanager.ui.config.HeaderReplacement;

import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class TombstoneUtils {
    public static Map<String,String> getReplacedTombstoneHeaders(Map<String,String> headers,
                                                           List<HeaderReplacement> replacements) {
        return headers.entrySet().stream()
                      .map(entry->new AbstractMap.SimpleEntry<>(entry.getKey(), getReplacedHeaderIfExists(entry, replacements)))
                      .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private static String getReplacedHeaderIfExists(Map.Entry<String,String> entry,
                                             List<HeaderReplacement> replacements) {
        return replacements.stream()
                           .filter(replacement -> replacement.getHeaderName().equals(entry.getKey())
                                   && nonNull(replacement.getReplacement().getValue()))
                           .map(replacement -> replacement.getReplacement().getValue().toString())
                           .findFirst()
                           .orElse(entry.getValue());
    }
}
