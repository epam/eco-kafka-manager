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
package com.epam.eco.kafkamanager.utils;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.common.security.auth.KafkaPrincipal;

/**
 * @author Andrei_Tytsik
 */
public class KafkaPrincipalComparator implements Comparator<KafkaPrincipal> {

    public static final KafkaPrincipalComparator INSTANCE = new KafkaPrincipalComparator();

    @Override
    public int compare(KafkaPrincipal o1, KafkaPrincipal o2) {
        int result = ObjectUtils.compare(o1.getPrincipalType(), o2.getPrincipalType());
        if (result == 0) {
            result = ObjectUtils.compare(o1.getName(), o2.getName());
        }
        return result;
    }

}

