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
package com.epam.eco.kafkamanager.rest.helper;

/**
 * @author Raman_Babich
 */
public final class PageImplFields {

    public static final String CONTENT_CLASS = "@contentClass";
    public static final String CONTENT = "content";
    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String TOTAL_PAGES = "totalPages";
    public static final String TOTAL_ELEMENTS = "totalElements";
    public static final String NUMBER_OF_ELEMENTS = "numberOfElements";
    public static final String PAGE = "page";

    private PageImplFields() {
    }
}
