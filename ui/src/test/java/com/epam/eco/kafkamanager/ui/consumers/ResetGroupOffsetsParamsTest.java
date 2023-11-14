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
package com.epam.eco.kafkamanager.ui.consumers;

import java.util.List;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Andrei_Tytsik
 */
public class ResetGroupOffsetsParamsTest {

    @Test
    public void testAccessorsGetDefaultsOnEmptyMap() throws Exception {
        ResetGroupOffsetsParams params = ResetGroupOffsetsParams.with(null);

        Assertions.assertNotNull(params);
        Assertions.assertNull(params.getGroupName());
        Assertions.assertNotNull(params.getPartitionOffsets());
        Assertions.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 0)));
        Assertions.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 1)));
        Assertions.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 2)));
        Assertions.assertTrue(params.isPartitionEnabled(new TopicPartition("topic", 0)));
        Assertions.assertTrue(params.isPartitionEnabled(new TopicPartition("topic", 1)));
        Assertions.assertTrue(params.isPartitionEnabled(new TopicPartition("topic", 2)));

        List<TopicPartition> partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertTrue(partitions.isEmpty());
    }

    @Test
    public void testAccessorsSetAndGetValues() throws Exception {
        ResetGroupOffsetsParams params = ResetGroupOffsetsParams.with(null);

        params.setGroupName("group");
        Assertions.assertEquals("group", params.getGroupName());

        TopicPartition partition0 = new TopicPartition("topic", 0);
        params.setPartitionEnabled(partition0, true);
        Assertions.assertTrue(params.isPartitionEnabled(partition0));

        TopicPartition partition1 = new TopicPartition("topic", 1);
        params.setPartitionEnabled(partition1, false);
        Assertions.assertFalse(params.isPartitionEnabled(partition1));

        TopicPartition partition2 = new TopicPartition("topic", 2);
        params.addPartitionOffset(partition2, 999);
        Assertions.assertTrue(params.containsPartition(partition2));
        Assertions.assertFalse(params.containsPartition(new TopicPartition("topic", 4)));
        Assertions.assertEquals(999, params.getPartitionOffset(partition2));

        List<TopicPartition> partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertEquals(1, partitions.size());

        TopicPartition partition4 = new TopicPartition("topic", 4);
        params.addPartitionOffset(partition4, 100);
        Assertions.assertTrue(params.containsPartition(partition4));
        Assertions.assertEquals(100, params.getPartitionOffset(partition4));

        partitions = params.listPartitions();
        Assertions.assertNotNull(partitions);
        Assertions.assertEquals(2, partitions.size());

        params.addPartitionOffsetOnCondition(partition4, 999, oldOffset -> oldOffset != 100);
        Assertions.assertNotEquals(999, params.getPartitionOffset(partition4));

        TopicPartition partition5 = new TopicPartition("topic", 5);
        params.addPartitionOffsetOnCondition(partition5, 333, oldOffset -> oldOffset == 0);
        Assertions.assertEquals(333, params.getPartitionOffset(partition5));
    }

}
