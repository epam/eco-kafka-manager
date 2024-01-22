package com.epam.eco.kafkamanager.ui.utils;
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

import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */
public class UiUtils {
    private static final int TRUNCATE_FIELD_LENGTH = 127;
    private static final String DUMMY_REPLACEMENT = "";

    private static final Pattern PATTERN_INLINE_SCRIPT = Pattern.compile("<script(.)*>(.)*</script([^\\S\t\n\r])*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REMOTE_SCRIPT_SHORT = Pattern.compile("<script(.)*/>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REMOTE_SCRIPT_FULL = Pattern.compile("<script(.)*</script([^\\S\t\n\r])*>", Pattern.CASE_INSENSITIVE);

    public static String getTruncatedDescription(String description) {
        return nonNull(description) ?
                description.substring(0,Math.min(TRUNCATE_FIELD_LENGTH,description.length())) :
                null;
    }

    public static String removeJsScripts(String text) {
        String result = PATTERN_INLINE_SCRIPT.matcher(text).replaceAll(DUMMY_REPLACEMENT);
        result = PATTERN_REMOTE_SCRIPT_SHORT.matcher(result).replaceAll(DUMMY_REPLACEMENT);
        result = PATTERN_REMOTE_SCRIPT_FULL.matcher(result).replaceAll(DUMMY_REPLACEMENT);
        return result;
    }
}
