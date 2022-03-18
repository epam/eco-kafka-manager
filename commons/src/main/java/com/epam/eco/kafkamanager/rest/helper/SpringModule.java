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
package com.epam.eco.kafkamanager.rest.helper;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Raman_Babich
 */
public class SpringModule extends SimpleModule {

    private static final long serialVersionUID = 5409219521537090773L;

    public SpringModule() {
        super(SpringModule.class.getSimpleName());

        addSerializer(new SortOrderJsonSerializer());
        addSerializer(new SortJsonSerializer());
        addSerializer(new PageRequestJsonSerializer());
        addSerializer(new PageImplJsonSerializer());

        addDeserializer(Sort.Order.class, new SortOrderJsonDeserializer());
        addDeserializer(Sort.class, new SortJsonDeserializer());
        addDeserializer(PageRequest.class, new PageRequestJsonDeserializer());
        addDeserializer(Pageable.class, new PageRequestJsonDeserializer(Pageable.class));
        addDeserializer(PageImpl.class, new PageImplJsonDeserializer());
    }
}
