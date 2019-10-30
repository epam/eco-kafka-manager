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
package com.epam.eco.kafkamanager.repo;

import java.util.List;

import com.epam.eco.kafkamanager.SearchQuery;

/**
 * @author Andrei_Tytsik
 */
public interface KeyValueRepo<K, V, Q extends SearchQuery<V>> extends ValueRepo<V, Q>  {
    boolean contains(K key);
    V get(K key);
    List<V> values(List<K> keys);
    List<K> keys();
}
