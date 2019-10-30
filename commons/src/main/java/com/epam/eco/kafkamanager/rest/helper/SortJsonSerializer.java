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
package com.epam.eco.kafkamanager.rest.helper;

import java.io.IOException;

import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Raman_Babich
 */
public class SortJsonSerializer extends StdSerializer<Sort> {

    private static final long serialVersionUID = -7967419238826239322L;

    public SortJsonSerializer() {
        super(Sort.class);
    }

    @Override
    public void serialize(Sort sort, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartArray();
        for (Sort.Order order : sort) {
            gen.writeObject(order);
        }
        gen.writeEndArray();
    }
}
