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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.epam.eco.commons.json.JsonDeserializerUtils;

/**
 * @author Raman_Babich
 */
public class SortOrderJsonDeserializer extends StdDeserializer<Sort.Order> {

    private static final long serialVersionUID = 7962115561982370136L;

    public SortOrderJsonDeserializer() {
        super(Sort.Order.class);
    }

    @Override
    public Sort.Order deserialize(
            JsonParser jsonParser,
            DeserializationContext ctxt) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            jsonParser.nextToken();
        }
        String fieldName = jsonParser.getCurrentName();

        String property = null;
        Sort.Direction direction = null;
        Sort.NullHandling nullHandling = null;
        Boolean ignoreCase = null;
        while (fieldName != null) {
            if (SortOrderFields.PROPERTY.equals(fieldName)) {
                jsonParser.nextToken();
                property = _parseString(jsonParser, ctxt);
            } else if (SortOrderFields.DIRECTION.equals(fieldName)) {
                jsonParser.nextToken();
                direction = jsonParser.readValueAs(Sort.Direction.class);
            } else if (SortOrderFields.NULL_HANDLING.equals(fieldName)) {
                jsonParser.nextToken();
                nullHandling = jsonParser.readValueAs(Sort.NullHandling.class);
            } else if (SortOrderFields.IGNORE_CASE.equals(fieldName)) {
                jsonParser.nextToken();
                ignoreCase = _parseBooleanPrimitive(jsonParser, ctxt);
            } else {
                handleUnknownProperty(jsonParser, ctxt, _valueClass, fieldName);
            }
            fieldName = jsonParser.nextFieldName();
        }

        JsonDeserializerUtils.assertNotNullValue(property, SortOrderFields.PROPERTY, _valueClass, ctxt);

        Sort.Order order;
        if (nullHandling == null) {
            order = new Sort.Order(direction, property);
        } else {
            order = new Sort.Order(direction, property, nullHandling);
        }
        if (ignoreCase != null && ignoreCase) {
            return order.ignoreCase();
        }
        return order;
    }

}
