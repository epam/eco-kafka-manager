/*******************************************************************************
 *  Copyright 2024 EPAM Systems
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
package com.epam.eco.kafkamanager.logicaltype;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */
public class LogicalTypeSchemaConverter {

    private static Predicate<Schema> isSchemaIsRecordPredicate() {
        return schema -> schema.getType().equals(Schema.Type.RECORD);
    }

    public static Map<String, Object> convert(GenericRecord value) {
        Map<String, Object> convertedValue = new HashMap<>();
        return convertSchema(value.getSchema(), value, convertedValue);
    }

    private static Map<String, Object> convertSchema(Schema schema,
                                                     GenericRecord fieldValue,
                                                     Map<String, Object> convertedValue
    ) {
        if(schema.hasFields()) {
            schema.getFields().forEach(field -> convertField(fieldValue, convertedValue, field));
        }
        return convertedValue;
    }

    private static void convertField(
            GenericRecord fieldValue,
            Map<String, Object> convertedValue,
            Schema.Field field
    ) {
        if(isNull(fieldValue)) {
            convertedValue.put(field.name(), null);
        } else if(isSchemaContainsRecord(field)) {
            convertRecordType(fieldValue, convertedValue, field);
        } else {
            convertLogicalType(fieldValue, convertedValue, field);
        }
    }

    private static boolean isSchemaContainsRecord(Schema.Field field) {
        if(field.schema().isUnion()) {
            return field.schema().getTypes().stream()
                    .anyMatch(isSchemaIsRecordPredicate());
        } else {
            return field.schema().getType().equals(Schema.Type.RECORD);
        }
    }

    private static void convertRecordType(
            GenericRecord fieldValue,
            Map<String, Object> convertedValue,
            Schema.Field field
    ) {
        GenericRecord fieldRecord = (GenericRecord) fieldValue.get(field.name());
        convertedValue.put(field.name(),
                           convertSchema(extractSchemaFromUnion(field, fieldRecord),
                                         fieldRecord,
                                         new HashMap<>()));
    }

    private static Schema extractSchemaFromUnion(Schema.Field field, GenericRecord fieldRecord) {
        if (fieldRecord != null) {
            return fieldRecord.getSchema();
        }
        if (field.schema().isUnion()) {
            return field.schema().getTypes().stream()
                    .filter(isSchemaIsRecordPredicate())
                    .findFirst()
                    .orElse(field.schema());
        } else {
            return field.schema();
        }
    }

    private static void convertLogicalType(
            GenericRecord fieldValue,
            Map<String, Object> convertedValue,
            Schema.Field field
    ) {
        if(isSchemaContainsLogicalType(field)) {
            convertedValue.put(field.name(),
                               LogicalTypeFieldConverter.convert(field.schema(),
                                                                 field.schema().getLogicalType(),
                                                                 fieldValue.get(field.name())) );
        } else {
            convertedValue.put(field.name(), fieldValue.get(field.name()));
        }
    }

    private static boolean isSchemaContainsLogicalType(Schema.Field field) {

        if(field.schema().getType().equals(Schema.Type.UNION)) {
            return field.schema().getTypes().stream()
                    .anyMatch(LogicalTypeSchemaConverter::isSchemaLogical);
        }
        return isSchemaLogical(field.schema());
    }

    private static boolean isSchemaLogical(Schema schema) {
        return nonNull(schema.getLogicalType()) ||
                (schema.getType() == Schema.Type.FIXED && schema.getFixedSize() == 12);
    }

}
