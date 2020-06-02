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

import com.epam.eco.kafkamanager.SearchCriteria;

/**
 * @author Andrei_Tytsik
 */
public abstract class AbstractKeyValueRepo<K, V, SC extends SearchCriteria<V>> extends AbstractValueRepo<V, SC> implements KeyValueRepo<K, V, SC> {
}
