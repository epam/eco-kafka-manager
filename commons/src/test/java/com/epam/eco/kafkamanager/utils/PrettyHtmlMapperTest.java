package com.epam.eco.kafkamanager.utils;

import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PrettyHtmlMapperTest {

    private final static String CORRECT_RESULT = "{<br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.exclusions</b>\": {<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion_doc_id</b>\": 433341, <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.exclusions.exclusions2</b>\": {<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion2_id</b>\": \"2345686-24521-1342\", <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion2_doc_id</b>\": 433452341<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}, <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\"<b>exclusion_id</b>\": \"UUID\"<br/>&nbsp;&nbsp;&nbsp;&nbsp;}, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.tt</b>\": 1546149600.000000000, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.doc_id</b>\": 4321, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.bt</b>\": 1546149600.000000000, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.lsn</b>\": 123454, <br/>&nbsp;&nbsp;&nbsp;&nbsp;\"<b>_datahub.metadata.operation_Id</b>\": \"12345678\"<br/>}";

    private static Map generateMap() {
        Map hashMap = new HashMap<>();
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

    @Test
    public void toPrettyHtmlTest() {
        String html = PrettyHtmlMapper.toPrettyHtml(generateMap());
        assertEquals(CORRECT_RESULT, html);

    }

}
