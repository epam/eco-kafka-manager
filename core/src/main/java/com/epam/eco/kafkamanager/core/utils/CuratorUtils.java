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

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

/**
 * @author Andrei_Tytsik
 */
public class CuratorUtils {

    public static boolean isConnectionStateChangeEvent(PathChildrenCacheEvent.Type type) {
        Validate.notNull(type, "Type is null");

        return
                PathChildrenCacheEvent.Type.CONNECTION_LOST == type ||
                PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED == type ||
                PathChildrenCacheEvent.Type.CONNECTION_SUSPENDED == type;
    }

    public static boolean isConnectionStateChangeEvent(TreeCacheEvent.Type type) {
        Validate.notNull(type, "Type is null");

        return
                TreeCacheEvent.Type.CONNECTION_LOST == type ||
                TreeCacheEvent.Type.CONNECTION_RECONNECTED == type ||
                TreeCacheEvent.Type.CONNECTION_SUSPENDED == type;
    }

}
