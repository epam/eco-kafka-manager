/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.browser.protobuf;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.epam.eco.kafkamanager.ui.config.HeaderReplacement;

/**
 * @author Mikhail_Vershkov
 */

public class TombstoneTestUtils {

    public static String TEST_DATE_1 = String.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
                                                                    .toInstant(ZoneOffset.UTC).toEpochMilli());
    public static String TEST_DATE_2 = String.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
                                                                   .toInstant(ZoneOffset.UTC).toEpochMilli());
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    public static List<HeaderReplacement> REPLACEMENTS = List.of(
            new HeaderReplacement("operation_type",
                                  PARSER.parseExpression("\"delete\"")),
            new HeaderReplacement("operation_time",
                                  PARSER.parseExpression("T(java.time.LocalDateTime).now().truncatedTo(T(java.time.temporal.ChronoUnit).HOURS).toInstant(T(java.time.ZoneOffset).UTC).toEpochMilli()")),
             new HeaderReplacement("business_time",
                                   PARSER.parseExpression("T(java.time.LocalDateTime).now().truncatedTo(T(java.time.temporal.ChronoUnit).DAYS).toInstant(T(java.time.ZoneOffset).UTC).toEpochMilli()")),
             new HeaderReplacement("null_header", null),
             new HeaderReplacement("wrong_header", PARSER.parseExpression("T(java.time.LocalDateTime).long()")),
             new HeaderReplacement("operation_type", PARSER.parseExpression("T(java.time.LocalDateTime).long()"))

                                                                );

    public static Map<String,String> TEST_HEADERS = Map.of("operation_type","merge",
                                                           "operation_time","1234567",
                                                           "business_time","654321",
                                                           "entity_type","applicant",
                                                           "repetitions","28");


}
