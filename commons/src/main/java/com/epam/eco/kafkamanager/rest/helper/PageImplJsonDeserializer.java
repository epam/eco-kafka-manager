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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.SimpleType;

import com.epam.eco.commons.json.JsonDeserializerUtils;

/**
 * @author Raman_Babich
 */
public class PageImplJsonDeserializer extends StdDeserializer<PageImpl<?>> implements ContextualDeserializer {

    private static final long serialVersionUID = 4703208267491574777L;

    private static final JavaType JAVA_OBJECT_TYPE = SimpleType.constructUnsafe(Object.class);
    private JavaType contentType = JAVA_OBJECT_TYPE;

    public PageImplJsonDeserializer() {
        super(PageImpl.class);
    }

    // TODO: Page interface has more than one implementation, but we are not take that into the account, for a simplicity.
    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType contextType;
        if (property != null) {
            contextType = property.getType();
        } else {
            contextType = ctxt.getContextualType();
        }
        PageImplJsonDeserializer deserializer = new PageImplJsonDeserializer();
        if (contextType == null) {
            return deserializer;
        }

        JavaType pageType = traverseByContentTypes(
                contextType, Stream.of(Page.class, PageImpl.class).collect(Collectors.toSet()));
        if (pageType == null) {
            ctxt.reportBadDefinition(
                    contextType,
                    String.format(
                            "Can't identify any type parts that are associated with '%s' class or '%s'",
                            Page.class.getName(), PageImpl.class.getName()));
        }
        if (pageType.hasGenericTypes()) {
            deserializer.contentType = pageType.containedType(0);
        }
        return deserializer;
    }

    private static JavaType traverseByContentTypes(JavaType root, Set<Class<?>> stopClasses) {
        JavaType currentType = root;
        while (true) {
            for (Class<?> clazz : stopClasses) {
                if (currentType.hasRawClass(clazz)) {
                    return currentType;
                }
            }

            if (currentType.isCollectionLikeType() || currentType.isMapLikeType()) {
                currentType = currentType.getContentType();
            } else {
                return null;
            }
        }
    }

    @Override
    public PageImpl<?> deserialize(
            JsonParser jsonParser,
            DeserializationContext ctxt) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            jsonParser.nextToken();
        }
        String fieldName = jsonParser.getCurrentName();

        JavaType contentClass = JAVA_OBJECT_TYPE;
        TreeNode contentNode = null;
        List<?> content = null;
        Long totalElements = null;
        Pageable pageable = null;
        while (fieldName != null) {
           if (PageImplFields.CONTENT_CLASS.equals(fieldName)) {
               jsonParser.nextToken();
               if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                   contentClass = jsonParser.readValueAs(JavaType.class);
               }
           } else if (PageImplFields.CONTENT.equals(fieldName)) {
               jsonParser.nextToken();
               contentNode = jsonParser.readValueAsTree();
           } else if (PageImplFields.TOTAL_ELEMENTS.equals(fieldName)) {
               jsonParser.nextToken();
               totalElements = _parseLongPrimitive(jsonParser, ctxt);
           } else if (PageImplFields.PAGE.equals(fieldName)) {
               jsonParser.nextToken();
               pageable = jsonParser.readValueAs(Pageable.class);
           } else if (
                   PageImplFields.FIRST.equals(fieldName) ||
                   PageImplFields.LAST.equals(fieldName) ||
                   PageImplFields.TOTAL_PAGES.equals(fieldName) ||
                   PageImplFields.NUMBER_OF_ELEMENTS.equals(fieldName)) {
               jsonParser.nextToken();
               //ignore
           } else {
               handleUnknownProperty(jsonParser, ctxt, _valueClass, fieldName);
           }
            fieldName = jsonParser.nextFieldName();
        }

        ObjectCodec codec = jsonParser.getCodec();
        if (contentNode != null) {
            JavaType targetType = listTypeWithElementsOf(contentType, ctxt);
            if (contentType.isJavaLangObject()) {
                targetType = listTypeWithElementsOf(contentClass, ctxt);
            }
            content = codec.readValue(contentNode.traverse(codec), targetType);
        }

        JsonDeserializerUtils.assertNotNullValue(content, PageImplFields.CONTENT, _valueClass, ctxt);

        if (pageable == null && totalElements == null) {
            return new PageImpl<>(content);
        }

        JsonDeserializerUtils.assertNotNullValue(pageable, PageImplFields.PAGE, _valueClass, ctxt);
        JsonDeserializerUtils.assertRequiredField(totalElements, PageImplFields.TOTAL_ELEMENTS, _valueClass, ctxt);

        return new PageImpl<>(content, pageable, totalElements);
    }

    private JavaType listTypeWithElementsOf(JavaType elemType, DeserializationContext ctxt) {
        return ctxt.getTypeFactory().constructCollectionType(List.class, elemType);
    }

}
