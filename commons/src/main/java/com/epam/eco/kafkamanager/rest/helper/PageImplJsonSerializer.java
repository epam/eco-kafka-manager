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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * @author Raman_Babich
 */
@SuppressWarnings("rawtypes")
public class PageImplJsonSerializer extends StdSerializer<PageImpl> implements ContextualSerializer {

    private static final long serialVersionUID = -3295877115071167316L;

    private static final JavaType JAVA_OBJECT_TYPE = SimpleType.constructUnsafe(Object.class);
    private JavaType contentType = JAVA_OBJECT_TYPE;

    public PageImplJsonSerializer() {
        super(PageImpl.class);
    }

    // TODO: Page interface has more than one implementation, but we are not take that into the account, for a simplicity.
    @Override
    public JsonSerializer<?> createContextual(
            SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        PageImplJsonSerializer serializer = new PageImplJsonSerializer();
        if (property == null) {
            return serializer;
        }
        JavaType pageType = traverseByContentTypes(
                property.getType(), Stream.of(Page.class, PageImpl.class).collect(Collectors.toSet()));
        if (pageType == null) {
            prov.reportBadDefinition(
                    property.getType(),
                    String.format(
                            "Can't identify any type parts that are associated with '%s' class or '%s'",
                            Page.class.getName(), PageImpl.class.getName()));
        }
        if (pageType.hasGenericTypes()) {
            serializer.contentType = pageType.containedType(0);
        }
        return serializer;
    }

    private static JavaType traverseByContentTypes(JavaType root, Set<Class<?>> stopClasses) {
        JavaType currentType = root;
        while (true) {
            for (Class clazz : stopClasses) {
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

    private static BeanProperty typeHolderBeanProperty(JavaType type) {
        return new BeanProperty.Std(PropertyName.NO_NAME, type, PropertyName.NO_NAME, null, null);
    }

    @Override
    public void serialize(
            PageImpl page,
            JsonGenerator gen,
            SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField(PageImplFields.CONTENT_CLASS, contentType);
        CollectionType contentListType = serializers.getTypeFactory().constructCollectionType(List.class, contentType);
        BeanProperty contentListProp = typeHolderBeanProperty(contentListType);
        JsonSerializer<Object> contentSerializer = serializers.findValueSerializer(contentListType, contentListProp);
        gen.writeFieldName(PageImplFields.CONTENT);
        contentSerializer.serialize(page.getContent(), gen, serializers);
        gen.writeNumberField(PageImplFields.TOTAL_ELEMENTS, page.getTotalElements());
        gen.writeBooleanField(PageImplFields.FIRST, page.isFirst());
        gen.writeBooleanField(PageImplFields.LAST, page.isLast());
        gen.writeNumberField(PageImplFields.TOTAL_PAGES, page.getTotalPages());
        gen.writeNumberField(PageImplFields.NUMBER_OF_ELEMENTS, page.getNumberOfElements());
        gen.writeObjectField(PageImplFields.PAGE, page.getPageable());
        gen.writeEndObject();
    }
}
