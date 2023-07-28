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
package com.epam.eco.kafkamanager.utils;

import org.apache.avro.util.Utf8;
import org.junit.jupiter.api.Test;


import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mikhail_Vershkov
 */
public class PrettyHtmlMapperTest {

    private static final String CORRECT_HTML_RESULT = "{<br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.exclusions</b>\" = {<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion_doc_id</b>\" = 433341, <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.exclusions.exclusions2</b>\" = {<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion2_id</b>\" = \"2345686-24521-1342\", <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion2_doc_id</b>\" = 433452341<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}, <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion_id</b>\" = \"UUID\"<br/>&nbsp;&nbsp;&nbsp;&nbsp;}, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.tt</b>\" = 1546149600.000000000, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.doc_id</b>\" = 4321, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.bt</b>\" = 1546149600.000000000, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.lsn</b>\" = 123454, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.operation_Id</b>\" = \"12345678\"<br/>}";
    private static final String CORRECT_STRING_RESULT = "{\n    \"_datahub.metadata.exclusions\" = {\n        \"exclusion_doc_id\" = 433341, \n        \"_datahub.metadata.exclusions.exclusions2\" = {\n            \"exclusion2_id\" = \"2345686-24521-1342\", \n            \"exclusion2_doc_id\" = 433452341\n        }, \n        \"exclusion_id\" = \"UUID\"\n    }, \n    \"_datahub.metadata.tt\" = 1546149600.000000000, \n    \"_datahub.metadata.doc_id\" = 4321, \n    \"_datahub.metadata.bt\" = 1546149600.000000000, \n    \"_datahub.metadata.lsn\" = 123454, \n    \"_datahub.metadata.operation_Id\" = \"12345678\"\n}";
    private static final String CORRECT_JSON_RESULT = "{\n    \"_datahub.metadata.exclusions\" : {\n        \"exclusion_doc_id\" : 433341, \n        \"_datahub.metadata.exclusions.exclusions2\" : {\n            \"exclusion2_id\" : \"2345686-24521-1342\", \n            \"exclusion2_doc_id\" : 433452341\n        }, \n        \"exclusion_id\" : \"UUID\"\n    }, \n    \"_datahub.metadata.tt\" : 1546149600.000000000, \n    \"_datahub.metadata.doc_id\" : 4321, \n    \"_datahub.metadata.bt\" : 1546149600.000000000, \n    \"_datahub.metadata.lsn\" : 123454, \n    \"_datahub.metadata.operation_Id\" : \"12345678\"\n}";

    private static Map<String, Object> generateMap() {
        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("_datahub.metadata.operation_Id", "12345678");
        hashMap.put("_datahub.metadata.doc_id", 4321);
        hashMap.put("_datahub.metadata.tt", OffsetDateTime.parse("2018-12-30T06:00:00Z"));
        hashMap.put("_datahub.metadata.bt", OffsetDateTime.parse("2018-12-30T06:00:00Z"));
        hashMap.put("_datahub.metadata.lsn", 123454);
        Map<String, Object> exclusions = new HashMap<>();
        exclusions.put("exclusion_id", "UUID");
        exclusions.put("exclusion_doc_id", 433341);
        hashMap.put("_datahub.metadata.exclusions", exclusions);
        Map<String, Object> exclusions2 = new HashMap<>();
        exclusions2.put("exclusion2_id", "2345686-24521-1342");
        exclusions2.put("exclusion2_doc_id", 433452341);
        exclusions.put("_datahub.metadata.exclusions.exclusions2", exclusions2);
        return hashMap;
    }

    private static Map<Utf8, Object> generateUtf8Map() {
        Map<Utf8,Object> hashMap = new HashMap<>();
        hashMap.put(new Utf8("_datahub.metadata.operation_Id"), new Utf8("12345678"));
        hashMap.put(new Utf8("_datahub.metadata.doc_id"), 4321);
        hashMap.put(new Utf8("_datahub.metadata.tt"), OffsetDateTime.parse("2018-12-30T06:00:00Z"));
        hashMap.put(new Utf8("_datahub.metadata.bt"), OffsetDateTime.parse("2018-12-30T06:00:00Z"));
        hashMap.put(new Utf8("_datahub.metadata.lsn"), 123454);
        Map<Utf8, Object> exclusions = new HashMap<>();
        exclusions.put(new Utf8("exclusion_id"), new Utf8("UUID"));
        exclusions.put(new Utf8("exclusion_doc_id"), 433341);
        hashMap.put(new Utf8("_datahub.metadata.exclusions"), exclusions);
        Map<Utf8, Object> exclusions2 = new HashMap<>();
        exclusions2.put(new Utf8("exclusion2_id"), new Utf8("2345686-24521-1342"));
        exclusions2.put(new Utf8("exclusion2_doc_id"), 433452341);
        exclusions.put(new Utf8("_datahub.metadata.exclusions.exclusions2"), exclusions2);
        return hashMap;
    }

    @Test
    public void toPrettyHtmlTest() {
        assertEquals(CORRECT_HTML_RESULT,
                     PrettyHtmlMapper.toPretty(generateMap(),PrettyHtmlMapper.PrettyFormat.HTML));
    }

    @Test
    public void toPrettyUtf8HtmlTest() {
        assertEquals(CORRECT_HTML_RESULT,
                     PrettyHtmlMapper.toPretty(generateUtf8Map(), PrettyHtmlMapper.PrettyFormat.HTML));
    }

    @Test
    public void toPrettyJsonTest() {
        assertEquals(CORRECT_JSON_RESULT,
                     PrettyHtmlMapper.toPretty(generateMap(),PrettyHtmlMapper.PrettyFormat.JSON));
    }

    @Test
    public void toPrettyUtf8JsonTest() {
        assertEquals(CORRECT_JSON_RESULT,
                     PrettyHtmlMapper.toPretty(generateUtf8Map(), PrettyHtmlMapper.PrettyFormat.JSON));
    }

    @Test
    public void toPrettyStringTest() {
        assertEquals(CORRECT_STRING_RESULT,
                     PrettyHtmlMapper.toPretty(generateMap(),PrettyHtmlMapper.PrettyFormat.STRING));
    }

    @Test
    public void toPrettyUtf8StringTest() {
        assertEquals(CORRECT_STRING_RESULT,
                     PrettyHtmlMapper.toPretty(generateUtf8Map(), PrettyHtmlMapper.PrettyFormat.STRING));
    }

}
