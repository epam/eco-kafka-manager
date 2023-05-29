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
package com.epam.eco.kafkamanager.ui.topics.browser.handlers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class FilterOperationUtils {

    private static final String LIKE_SQL_STYLE = "%";
    private static final String LIKE_JAVA_STYLE = "(.)+";

    public static boolean like(String regexp, String testString) {
        if(isNull(testString) || testString.isEmpty()) {
            return false;
        }
        return testString.matches(regexp.replaceAll(LIKE_SQL_STYLE, LIKE_JAVA_STYLE));
    }
    public static boolean notEmpty(String filter, String testString) {
        return nonNull(testString) && !testString.isEmpty();
    }

    public static String strip(String value) {
        return value
                .replace("\n", "")
                .replace("\r", "")
                .trim();

    }
}
