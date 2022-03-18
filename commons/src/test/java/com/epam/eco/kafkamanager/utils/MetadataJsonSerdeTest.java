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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.commons.kafka.serde.JsonDeserializer;
import com.epam.eco.commons.kafka.serde.JsonSerializer;
import com.epam.eco.kafkamanager.Metadata;

/**
 * @author Andrei_Tytsik
 */
public class MetadataJsonSerdeTest {

    private JsonSerializer serializer = new JsonSerializer();
    private JsonDeserializer<Metadata> deserializer = new JsonDeserializer<>();
    {
        deserializer.configure(
                Collections.singletonMap(JsonDeserializer.VALUE_TYPE, Metadata.class),
                false);
    }

    @Test
    public void testMetadataIsSerializedAndDeserialized() throws Exception {
        Metadata metadataOrig = Metadata.builder().
                description("Description").
                appendAttribute("attr1", "attr1").
                appendAttribute("attr2", new ArrayList<>()).
                appendAttribute("attr3", 42).
                appendAttribute("attr4", .999).
                appendAttribute("attr5", new HashMap<>()).
                build();

        byte[] bytes = serializer.serialize(null, metadataOrig);

        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        Metadata metadata = deserializer.deserialize(null, bytes);

        Assert.assertEquals(metadataOrig, metadata);
    }

}
