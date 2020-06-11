/*
 * Copyright 2020 EPAM Systems
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.epam.eco.kafkamanager.SearchCriteria;

/**
 * @author Andrei_Tytsik
 */
public abstract class AbstractValueRepo<V, SC extends SearchCriteria<V>> implements ValueRepo<V, SC> {

    @Override
    public Page<V> page(Pageable pageable) {
        return page(null, pageable);
    }

    @Override
    public Page<V> page(SC criteria, Pageable pageable) {
        Validate.notNull(pageable, "Pageable is null");

        List<V> allValues = applyCriteriaIfPresented(values(), criteria);
        List<V> pageValues = new ArrayList<>();
        int idx = 0;
        for (V value : allValues) {
            if (idx >= pageable.getOffset()) {
                pageValues.add(value);
                if (pageValues.size() == pageable.getPageSize()) {
                    break;
                }
            }
            idx++;
        }
        return new PageImpl<>(pageValues, pageable, allValues.size());
    }

    @Override
    public List<V> values(SC criteria) {
        return applyCriteriaIfPresented(values(), criteria);
    }

    protected List<V> applyCriteriaIfPresented(List<V> values, SC criteria) {
        if (criteria == null) {
            return values;
        }

        return values.stream().filter(criteria::matches).collect(Collectors.toList());
    }

}
