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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfigRepo.UpdateListener;
import com.epam.eco.kafkamanager.udmetrics.utils.MetricNameUtils;


/**
 * @author Andrei_Tytsik
 */
public class UDMetricManagerImpl implements UDMetricManager, UpdateListener {

    @Autowired
    private KafkaManager kafkaManager;

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private UDMetricConfigRepo configRepo;

    private final Map<String, UDMetric> udmRegistry = new TreeMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    private void init() {
        subscribeOnConfigRepoUpdates();
    }

    private void subscribeOnConfigRepoUpdates() {
        configRepo.addUpdateListener(this);
    }

    @Override
    public UDMetric createOrReplace(
            UDMetricType type,
            String resourceName,
            Map<String, Object> config) {
        Validate.notNull(type, "UDM type is null");
        Validate.notBlank(resourceName, "Resource name is blank");

        UDMetricConfig udmConfig = UDMetricConfig.with(
                type.formatMetricName(resourceName),
                type,
                resourceName,
                config);

        return createAndRegister(udmConfig, true);
    }

    @Override
    public void remove(String name) {
        Validate.notBlank(name, "UDM name is blank");

        lock.writeLock().lock();
        try {
            unregisterUDMByName(name);

            configRepo.remove(name);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public UDMetric get(String name) {
        Validate.notBlank(name, "UDM name is blank");

        lock.readLock().lock();
        try {
            return udmRegistry.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Page<UDMetric> page(Pageable pageable) {
        return page(null, pageable);
    }

    @Override
    public Page<UDMetric> page(UDMetricSearchQuery query, Pageable pageable) {
        Validate.notNull(pageable, "Pageable is null");

        List<UDMetric> allUdms = applyQueryIfPresented(listAll(), query);
        List<UDMetric> pageUdms = new ArrayList<>();
        int idx = 0;
        for (UDMetric udm : allUdms) {
            if (idx >= pageable.getOffset()) {
                pageUdms.add(udm);
                if (pageUdms.size() == pageable.getPageSize()) {
                    break;
                }
            }
            idx++;
        }
        return new PageImpl<>(pageUdms, pageable, allUdms.size());
    }

    @Override
    public List<UDMetric> listAll() {
        lock.readLock().lock();
        try {
            return udmRegistry.values().stream().
                    sorted().
                    collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getCount() {
        lock.readLock().lock();
        try {
            return udmRegistry.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    protected List<UDMetric> applyQueryIfPresented(List<UDMetric> values, UDMetricSearchQuery query) {
        if (query == null) {
            return values;
        }

        return values.stream().filter(query::matches).collect(Collectors.toList());
    }

    private UDMetric createAndRegister(UDMetricConfig config, boolean persist) {
        lock.writeLock().lock();
        try {
            UDMetric udm = createUDM(config);

            registerUDM(udm);

            if (persist) {
                configRepo.createOrReplace(config);
            }

            return udm;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void onConfigUpdated(String name, UDMetricConfig config) {
        createAndRegister(config, false);
    }

    @Override
    public void onConfigRemoved(String name) {
        remove(name);
    }

    private UDMetric createUDM(UDMetricConfig config) {
        UDMetricCreator creator = config.getType().creator();
        try {
            Map<String, Metric> metrics = creator.create(config, kafkaManager);
            return UDMetric.with(config, metrics);
        } catch (Exception ex) {
            return UDMetric.with(
                    config,
                    Collections.singletonList(ex.getMessage()));
        }
    }

    private void registerUDM(UDMetric udm) {
        unregisterUDMByName(udm.getName());

        if (!udm.hasErrors()) {
            metricRegistry.register(udm.getName(), udm);
        }

        udmRegistry.put(udm.getName(), udm);
    }

    private void unregisterUDMByName(String name) {
        UDMetric udm = udmRegistry.get(name);
        if (udm == null) {
            return;
        }

        Set<String> concatenatedNames = MetricNameUtils.extractConcatenatedNames(name, udm);
        if (concatenatedNames != null) {
            concatenatedNames.forEach(metricRegistry::remove);
        }

        udmRegistry.remove(name);
    }

}
