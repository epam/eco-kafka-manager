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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Raman_Babich
 */
public class SortJsonDeserializer extends StdDeserializer<Sort> {

    private static final long serialVersionUID = 8590844623338195093L;

    public SortJsonDeserializer() {
        super(Sort.class);
    }

    @Override
    public Sort deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            ctxt.reportWrongTokenException(
                    _valueClass,
                    jsonParser.getCurrentToken(),
                    "Input should start with '%s' token", JsonToken.START_ARRAY.asString());
        }
        jsonParser.nextToken();
        if (jsonParser.getCurrentToken() == JsonToken.END_ARRAY) {
            return Sort.by(Collections.emptyList());
        }
        List<Sort.Order> headers = toList(jsonParser.readValuesAs(Sort.Order.class));
        return Sort.by(headers);
    }

    private static  <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

}
