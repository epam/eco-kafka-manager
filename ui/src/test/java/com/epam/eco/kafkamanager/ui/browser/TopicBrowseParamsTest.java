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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.ui.topics.browser.TopicBrowseParams;

/**
 * @author Andrei_Tytsik
 */
public class TopicBrowseParamsTest {

    @Test
    public void testAccessorsGetDefaultsOnEmptyMap() throws Exception {
        TopicBrowseParams params = TopicBrowseParams.with(null);

        Assertions.assertNotNull(params);
        Assertions.assertNull(params.getTopicName());
        Assertions.assertNull(params.getKeyFormat());
        Assertions.assertNull(params.getValueFormat());
        Assertions.assertNotNull(params.getPartitionOffsets());
        Assertions.assertTrue(params.getLimit() > 0);
        Assertions.assertEquals(0, params.getPartitionOffset(0).getSize());
        Assertions.assertEquals(0, params.getPartitionOffset(1).getSize());
        Assertions.assertEquals(0, params.getPartitionOffset(2).getSize());
        Assertions.assertTrue(params.isPartitionEnabled(0));
        Assertions.assertTrue(params.isPartitionEnabled(1));
        Assertions.assertTrue(params.isPartitionEnabled(2));

        List<Integer> partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertTrue(partitions.isEmpty());
    }

    @Test
    public void testAccessorsSetAndGetValues() throws Exception {
        TopicBrowseParams params = TopicBrowseParams.with(null);

        params.setTopicName("topic");
        Assertions.assertEquals("topic", params.getTopicName());

        params.setKeyFormat(DataFormat.JSON_STRING);
        Assertions.assertEquals(DataFormat.JSON_STRING, params.getKeyFormat());

        params.setKeyFormat("STRING");
        Assertions.assertEquals(DataFormat.STRING, params.getKeyFormat());

        params.deleteKeyFormat();
        Assertions.assertEquals(null, params.getKeyFormat());

        params.setKeyFormatIfMissing(DataFormat.AVRO);
        Assertions.assertEquals(DataFormat.AVRO, params.getKeyFormat());

        params.setValueFormat(DataFormat.AVRO);
        Assertions.assertEquals(DataFormat.AVRO, params.getValueFormat());

        params.setValueFormat("HEX_STRING");
        Assertions.assertEquals(DataFormat.HEX_STRING, params.getValueFormat());

        params.deleteValueFormat();
        Assertions.assertEquals(null, params.getValueFormat());

        params.setValueFormatIfMissing(DataFormat.AVRO);
        Assertions.assertEquals(DataFormat.AVRO, params.getValueFormat());

        params.setTimeout(10000);
        Assertions.assertEquals(10000, params.getTimeout());

        params.setLimit(10);
        Assertions.assertEquals(10, params.getLimit());

        params.setPartitionEnabled(1, true);
        Assertions.assertEquals(true, params.isPartitionEnabled(1));

        params.setPartitionEnabled(1, false);
        Assertions.assertEquals(false, params.isPartitionEnabled(1));

        params.addPartitionOffset(0, OffsetRange.with(0,999, true));
        Assertions.assertTrue(params.containsPartition(0));
        Assertions.assertFalse(params.containsPartition(1));
        Assertions.assertEquals(999, params.getPartitionOffset(0).getSize()-1);

        List<Integer> partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertEquals(1, partitions.size());
        Assertions.assertFalse(params.containsPartition(2));
        Assertions.assertEquals(1000, params.getPartitionOffset(partitions.get(0)).getSize());

        params.addPartitionOffset(1, OffsetRange.with(0,100, true));
        Assertions.assertTrue(params.containsPartition(1));
        Assertions.assertEquals(100, params.getPartitionOffset(1).getSize()-1);

        partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertEquals(2, partitions.size());
        Assertions.assertEquals(100, params.getPartitionOffset(partitions.get(1)).getSize()-1);

        params.addPartitionOffsetOnCondition(1, 999, oldOffset -> oldOffset.getLargest() != 100);
        Assertions.assertNotEquals(999, params.getPartitionOffset(1));

        //        params.addPartitionOffsetOnCondition(2, 333, oldOffset -> oldOffset.getSmallest() == 0);
        //        Assertions.assertEquals(333, params.getPartitionOffset(2).getSize()-1);

        Assertions.assertEquals(Collections.emptyList(), params.listColumns());

        params.setColumnEnabled("a", true);
        Assertions.assertEquals(true, params.isColumnEnabled("a"));
        params.setColumnEnabled("b", false);
        Assertions.assertEquals(false, params.isColumnEnabled("b"));
        Assertions.assertEquals(Arrays.asList(new String[]{"a"}), params.listColumns());

        params.setColumnEnabled("b", true);
        Assertions.assertEquals(true, params.isColumnEnabled("b"));
        Assertions.assertEquals(Arrays.asList(new String[]{"a","b"}), params.listColumns());
    }

}
