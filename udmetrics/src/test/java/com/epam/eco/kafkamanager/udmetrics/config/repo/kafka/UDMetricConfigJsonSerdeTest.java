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
package com.epam.eco.kafkamanager.udmetrics.config.repo.kafka;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.commons.kafka.serde.JsonDeserializer;
import com.epam.eco.commons.kafka.serde.JsonSerializer;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfig;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;

/**
 * @author Andrei_Tytsik
 */
public class UDMetricConfigJsonSerdeTest {

    private JsonSerializer serializer = new JsonSerializer();
    private JsonDeserializer<UDMetricConfig> deserializer = new JsonDeserializer<>();
    {
        deserializer.configure(
                Collections.singletonMap(JsonDeserializer.KEY_TYPE, UDMetricConfig.class),
                true);
    }

    @Test
    public void testConfigIsSerializedAndDeserialized() throws Exception {
        UDMetricConfig configOrig = UDMetricConfig.with(
                "name",
                UDMetricType.CONSUMER_GROUP_LAG,
                "recourceName",
                Collections.emptyMap());

        byte[] bytes = serializer.serialize(null, configOrig);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        UDMetricConfig config = deserializer.deserialize(null, bytes);
        Assert.assertNotNull(config);
        Assert.assertEquals(configOrig, config);
    }

}
