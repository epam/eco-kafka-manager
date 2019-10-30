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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Andrei_Tytsik
 */
public interface UDMetricManager {
    UDMetric createOrReplace(
            UDMetricType type,
            String resourceName,
            Map<String, Object> config);
    void remove(String name);
    UDMetric get(String name);
    Page<UDMetric> page(Pageable pageable);
    Page<UDMetric> page(UDMetricSearchQuery query, Pageable pageable);
    List<UDMetric> listAll();
    int getCount();
}
