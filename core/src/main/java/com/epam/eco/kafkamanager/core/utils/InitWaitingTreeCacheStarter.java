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

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * @author Andrei_Tytsik
 */
public class InitWaitingTreeCacheStarter {

    private final TreeCache treeCache;
    private final String rootPath;

    public InitWaitingTreeCacheStarter(TreeCache treeCache, String rootPath) {
        Validate.notNull(treeCache, "Tree cache is null");
        Validate.notBlank(rootPath, "Root path is blank");

        this.treeCache = treeCache;
        this.rootPath = rootPath;
    }

    public static InitWaitingTreeCacheStarter with(TreeCache treeCache, String rootPath) {
        return new InitWaitingTreeCacheStarter(treeCache, rootPath);
    }

    public void start() throws Exception {
        InitWaitingListener initWaitingListener = new InitWaitingListener();
        try {
            treeCache.getListenable().addListener(initWaitingListener);
            treeCache.start();
            treeCache.getCurrentData(rootPath);
            initWaitingListener.awaitInitialization();
        } finally {
            treeCache.getListenable().removeListener(initWaitingListener);
        }
    }

    private static class InitWaitingListener implements TreeCacheListener {

        private final CountDownLatch initializedLatch = new CountDownLatch(1);

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            Validate.notNull(event, "Event can't be null");

            if (event.getType() == Type.INITIALIZED) {
                initializedLatch.countDown();
            }
        }

        public void awaitInitialization() throws InterruptedException {
            initializedLatch.await();
        }

    }

}
