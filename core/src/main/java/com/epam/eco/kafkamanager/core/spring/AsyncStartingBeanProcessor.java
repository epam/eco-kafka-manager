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
package com.epam.eco.kafkamanager.core.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;


/**
 * @author Andrei_Tytsik
 */
public class AsyncStartingBeanProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncStartingBeanProcessor.class);

    @Autowired(required=false)
    private List<AsyncStartingBean> beans;

    @EventListener(ContextRefreshedEvent.class)
    public void startBeans() throws Exception {
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(beans.size());
        List<Callable<Object>> tasks = new ArrayList<>(beans.size());
        for (AsyncStartingBean bean : beans) {
            tasks.add(() -> {
                LOGGER.info("Starting bean {} asyncronously", bean);
                bean.startAsync();
                return null;
            });
        }
        executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

}
