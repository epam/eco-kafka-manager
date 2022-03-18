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

import java.io.IOException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.epam.eco.commons.json.JsonDeserializerUtils;

/**
 * @author Raman_Babich
 */
public class PageRequestJsonDeserializer extends StdDeserializer<PageRequest> {

    private static final long serialVersionUID = -569153358775462685L;

    public PageRequestJsonDeserializer() {
        super(PageRequest.class);
    }

    public PageRequestJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PageRequest deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            jsonParser.nextToken();
        }
        String fieldName = jsonParser.getCurrentName();

        Integer number = null;
        Integer size = null;
        Sort sort = null;
        while (fieldName != null) {
            if (PageRequestFields.NUMBER.equals(fieldName)) {
                jsonParser.nextToken();
                number = _parseIntPrimitive(jsonParser, ctxt);
            } else if (PageRequestFields.SIZE.equals(fieldName)) {
                jsonParser.nextToken();
                size = _parseIntPrimitive(jsonParser, ctxt);
            } else if (PageRequestFields.SORT.equals(fieldName)) {
                jsonParser.nextToken();
                sort = jsonParser.readValueAs(Sort.class);
            } else {
                handleUnknownProperty(jsonParser, ctxt, _valueClass, fieldName);
            }
            fieldName = jsonParser.nextFieldName();
        }

        JsonDeserializerUtils.assertRequiredField(number, PageRequestFields.NUMBER, _valueClass, ctxt);
        JsonDeserializerUtils.assertRequiredField(size, PageRequestFields.SIZE, _valueClass, ctxt);

        if (sort == null) {
            return PageRequest.of(number, size);
        }
        return PageRequest.of(number, size, sort);
    }

}
