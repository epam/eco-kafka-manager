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
package com.epam.eco.kafkamanager.ui.browser;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.TombstoneUtils;

import static com.epam.eco.kafkamanager.ui.browser.protobuf.TombstoneTestUtils.REPLACEMENTS;
import static com.epam.eco.kafkamanager.ui.browser.protobuf.TombstoneTestUtils.TEST_DATE_1;
import static com.epam.eco.kafkamanager.ui.browser.protobuf.TombstoneTestUtils.TEST_DATE_2;
import static com.epam.eco.kafkamanager.ui.browser.protobuf.TombstoneTestUtils.TEST_HEADERS;

/**
 * @author Mikhail_Vershkov
 */

public class TombstonesTest {

    @Test
    void headerReplacingTest() {
        Map<String,String> map = TombstoneUtils.getReplacedTombstoneHeaders(TEST_HEADERS, REPLACEMENTS);
        Assertions.assertEquals(5,map.size());
        Assertions.assertEquals("delete", map.get("operation_type"));
        Assertions.assertEquals(map.get("operation_type"),"delete");
        Assertions.assertEquals(map.get("operation_time"),TEST_DATE_1);
        Assertions.assertEquals(map.get("business_time"), TEST_DATE_2);
        Assertions.assertEquals(map.get("entity_type"),"applicant");
        Assertions.assertEquals(map.get("repetitions"),"28");
    }
}
