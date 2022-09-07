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
package com.epam.eco.kafkamanager.ui.browser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.ui.topics.browser.TopicBrowseParams;

/**
 * @author Andrei_Tytsik
 */
public class TopicBrowseParamsTest {

    @Test
    public void testAccessorsGetDefaultsOnEmptyMap() throws Exception {
        TopicBrowseParams params = TopicBrowseParams.with(null);

        Assert.assertNotNull(params);
        Assert.assertEquals(null, params.getTopicName());
        Assert.assertEquals(null, params.getKeyFormat());
        Assert.assertEquals(null, params.getValueFormat());
        Assert.assertNotNull(params.getPartitionOffsets());
        Assert.assertTrue(params.getLimit() > 0);
        Assert.assertEquals(0, params.getPartitionOffset(0));
        Assert.assertEquals(0, params.getPartitionOffset(1));
        Assert.assertEquals(0, params.getPartitionOffset(2));
        Assert.assertEquals(true, params.isPartitionEnabled(0));
        Assert.assertEquals(true, params.isPartitionEnabled(1));
        Assert.assertEquals(true, params.isPartitionEnabled(2));

        List<Integer> partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertTrue(partitions.isEmpty());
    }

    @Test
    public void testAccessorsSetAndGetValues() throws Exception {
        TopicBrowseParams params = TopicBrowseParams.with(null);

        params.setTopicName("topic");
        Assert.assertEquals("topic", params.getTopicName());

        params.setKeyFormat(DataFormat.JSON_STRING);
        Assert.assertEquals(DataFormat.JSON_STRING, params.getKeyFormat());

        params.setKeyFormat("STRING");
        Assert.assertEquals(DataFormat.STRING, params.getKeyFormat());

        params.deleteKeyFormat();
        Assert.assertEquals(null, params.getKeyFormat());

        params.setKeyFormatIfMissing(DataFormat.AVRO);
        Assert.assertEquals(DataFormat.AVRO, params.getKeyFormat());

        params.setValueFormat(DataFormat.AVRO);
        Assert.assertEquals(DataFormat.AVRO, params.getValueFormat());

        params.setValueFormat("HEX_STRING");
        Assert.assertEquals(DataFormat.HEX_STRING, params.getValueFormat());

        params.deleteValueFormat();
        Assert.assertEquals(null, params.getValueFormat());

        params.setValueFormatIfMissing(DataFormat.AVRO);
        Assert.assertEquals(DataFormat.AVRO, params.getValueFormat());

        params.setTimeout(10000);
        Assert.assertEquals(10000, params.getTimeout());

        params.setLimit(10);
        Assert.assertEquals(10, params.getLimit());

        params.setPartitionEnabled(1, true);
        Assert.assertEquals(true, params.isPartitionEnabled(1));

        params.setPartitionEnabled(1, false);
        Assert.assertEquals(false, params.isPartitionEnabled(1));

        params.addPartitionOffset(0, 999);
        Assert.assertTrue(params.containsPartition(0));
        Assert.assertFalse(params.containsPartition(1));
        Assert.assertEquals(999, params.getPartitionOffset(0));

        List<Integer> partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertEquals(1, partitions.size());
        Assert.assertFalse(params.containsPartition(2));
        Assert.assertEquals(999, params.getPartitionOffset(partitions.get(0)));

        params.addPartitionOffset(1, 100);
        Assert.assertTrue(params.containsPartition(1));
        Assert.assertEquals(100, params.getPartitionOffset(1));

        partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertEquals(2, partitions.size());
        Assert.assertEquals(100, params.getPartitionOffset(partitions.get(1)));

        params.addPartitionOffsetOnCondition(1, 999, oldOffset -> oldOffset != 100);
        Assert.assertNotEquals(999, params.getPartitionOffset(1));

        params.addPartitionOffsetOnCondition(2, 333, oldOffset -> oldOffset == 0);
        Assert.assertEquals(333, params.getPartitionOffset(2));

        Assert.assertEquals(Collections.emptyList(), params.listColumns());

        params.setColumnEnabled("a", true);
        Assert.assertEquals(true, params.isColumnEnabled("a"));
        params.setColumnEnabled("b", false);
        Assert.assertEquals(false, params.isColumnEnabled("b"));
        Assert.assertEquals(Arrays.asList(new String[]{"a"}), params.listColumns());

        params.setColumnEnabled("b", true);
        Assert.assertEquals(true, params.isColumnEnabled("b"));
        Assert.assertEquals(Arrays.asList(new String[]{"a","b"}), params.listColumns());
    }

}
