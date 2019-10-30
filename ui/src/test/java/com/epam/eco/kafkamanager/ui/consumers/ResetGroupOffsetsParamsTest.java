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
package com.epam.eco.kafkamanager.ui.consumers;

import java.util.List;

import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.kafkamanager.ui.consumers.ResetGroupOffsetsParams;

/**
 * @author Andrei_Tytsik
 */
public class ResetGroupOffsetsParamsTest {

    @Test
    public void testAccessorsGetDefaultsOnEmptyMap() throws Exception {
        ResetGroupOffsetsParams params = ResetGroupOffsetsParams.with(null);

        Assert.assertNotNull(params);
        Assert.assertEquals(null, params.getGroupName());
        Assert.assertNotNull(params.getPartitionOffsets());
        Assert.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 0)));
        Assert.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 1)));
        Assert.assertEquals(0, params.getPartitionOffset(new TopicPartition("topic", 2)));
        Assert.assertEquals(true, params.isPartitionEnabled(new TopicPartition("topic", 0)));
        Assert.assertEquals(true, params.isPartitionEnabled(new TopicPartition("topic", 1)));
        Assert.assertEquals(true, params.isPartitionEnabled(new TopicPartition("topic", 2)));

        List<TopicPartition> partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertTrue(partitions.isEmpty());
    }

    @Test
    public void testAccessorsSetAndGetValues() throws Exception {
        ResetGroupOffsetsParams params = ResetGroupOffsetsParams.with(null);

        params.setGroupName("group");
        Assert.assertEquals("group", params.getGroupName());

        TopicPartition partition0 = new TopicPartition("topic", 0);
        params.setPartitionEnabled(partition0, true);
        Assert.assertEquals(true, params.isPartitionEnabled(partition0));

        TopicPartition partition1 = new TopicPartition("topic", 1);
        params.setPartitionEnabled(partition1, false);
        Assert.assertEquals(false, params.isPartitionEnabled(partition1));

        TopicPartition partition2 = new TopicPartition("topic", 2);
        params.addPartitionOffset(partition2, 999);
        Assert.assertTrue(params.containsPartition(partition2));
        Assert.assertFalse(params.containsPartition(new TopicPartition("topic", 4)));
        Assert.assertEquals(999, params.getPartitionOffset(partition2));

        List<TopicPartition> partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertEquals(1, partitions.size());

        TopicPartition partition4 = new TopicPartition("topic", 4);
        params.addPartitionOffset(partition4, 100);
        Assert.assertTrue(params.containsPartition(partition4));
        Assert.assertEquals(100, params.getPartitionOffset(partition4));

        partitions = params.listPartitions();
        Assert.assertNotNull(partitions);
        Assert.assertEquals(2, partitions.size());

        params.addPartitionOffsetOnCondition(partition4, 999, oldOffset -> oldOffset != 100);
        Assert.assertNotEquals(999, params.getPartitionOffset(partition4));

        TopicPartition partition5 = new TopicPartition("topic", 5);
        params.addPartitionOffsetOnCondition(partition5, 333, oldOffset -> oldOffset == 0);
        Assert.assertEquals(333, params.getPartitionOffset(partition5));
    }

}
