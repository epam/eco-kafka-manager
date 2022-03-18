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
package com.epam.eco.kafkamanager.core.utils;

import java.net.UnknownHostException;

import org.apache.curator.utils.DefaultZookeeperFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Workaround for https://issues.apache.org/jira/browse/CURATOR-229
 *
 * @author Andrei_Tytsik
 */
public class RetriableZookeeperFactory extends DefaultZookeeperFactory {

    @Override
    public ZooKeeper newZooKeeper(
            String connectString,
            int sessionTimeout,
            Watcher watcher,
            boolean canBeReadOnly) throws Exception {
        try {
            return super.newZooKeeper(connectString, sessionTimeout, watcher, canBeReadOnly);
        } catch (UnknownHostException ex) { // valid for ZK <= 3.4.10
            throw new KeeperException.ConnectionLossException();
        }
    }

}
