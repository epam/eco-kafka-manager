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
package com.epam.eco.kafkamanager.rest.request;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class MetadataRequestTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        MetadataRequest origin = new MetadataRequest("Description", Collections.singletonMap("a", "b"));

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(origin);
        Assertions.assertNotNull(json);

        MetadataRequest deserialized = mapper.readValue(
                json,
                MetadataRequest.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(deserialized, origin);
    }
}
