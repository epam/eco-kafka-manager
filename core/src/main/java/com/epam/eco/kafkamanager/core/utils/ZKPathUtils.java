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
package com.epam.eco.kafkamanager.core.utils;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.curator.utils.ZKPaths;

/**
 * @author Andrei_Tytsik
 */
public class ZKPathUtils {

    public static String getPathToken(String path, int index) {
        Validate.notNull(path, "Path can't be null");

        List<String> tokens = ZKPaths.split(path);
        if (tokens.size() <= index) {
            throw new IllegalArgumentException(
                    String.format("Can't extract #%d token from path %s", index, path));
        }

        return tokens.get(index);
    }

}
