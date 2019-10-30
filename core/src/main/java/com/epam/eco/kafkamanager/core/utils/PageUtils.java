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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Raman_Babich
 */
public final class PageUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public static <T> Page<T> pageFrom(List<T> data, Pageable pageable) {
        Validate.notNull(data, "Data can't be null");

        int start = (int)pageable.getOffset();
        int end = (start + pageable.getPageSize()) > data.size() ? data.size() : (start + pageable.getPageSize());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }

    public static Pageable buildPageableWithDefaultsIfNull(
            Integer page,
            Integer pageSize) {
        return buildPageable(page, DEFAULT_PAGE, pageSize, DEFAULT_PAGE_SIZE);
    }

    public static Pageable buildPageable(
            Integer page,
            Integer nullDefaultPage,
            Integer pageSize,
            Integer nullDefaultPageSize) {
        page = page != null ? page : nullDefaultPage;
        pageSize = pageSize != null ? pageSize : nullDefaultPageSize;

        Validate.notNull(page, "Page is null");
        Validate.notNull(pageSize, "Page size is null");

        return PageRequest.of(page, pageSize);
    }

    private PageUtils() {
    }

}
