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
package com.epam.eco.kafkamanager.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.kafkamanager.BrokerConfigUpdateParams;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataDeleteParams;
import com.epam.eco.kafkamanager.BrokerMetadataUpdateParams;
import com.epam.eco.kafkamanager.BrokerSearchCriteria;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.core.utils.PageUtils;
import com.epam.eco.kafkamanager.rest.request.BrokerConfigRequest;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/brokers")
public class BrokerController {

    @Autowired
    private KafkaManager kafkaManager;

    @GetMapping
    public Page<BrokerInfo> getBrokerPage(
            @RequestParam(value = "brokerId", required = false) Integer brokerId,
            @RequestParam(value = "rack", required = false) String rack,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = PageUtils.buildPageableWithDefaultsIfNull(page, pageSize);
        BrokerSearchCriteria query = BrokerSearchCriteria.builder()
                .brokerId(brokerId)
                .rack(rack)
                .description(description)
                .build();
        return kafkaManager.getBrokerPage(query, pageable);
    }

    @GetMapping("/{id}")
    public BrokerInfo getBroker(@PathVariable("id") Integer id) {
        return kafkaManager.getBroker(id);
    }

    @PutMapping("/{id}/metadata")
    public BrokerInfo putBrokerMetadata(
            @PathVariable("id") Integer id,
            @RequestBody MetadataRequest request) {
        BrokerMetadataUpdateParams params = BrokerMetadataUpdateParams.builder()
                .brokerId(id)
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        return kafkaManager.updateBroker(params);
    }

    @DeleteMapping("/{id}/metadata")
    public BrokerInfo deletBrokerMetadata(@PathVariable("id") Integer id) {
        BrokerMetadataDeleteParams params = BrokerMetadataDeleteParams.builder()
                .brokerId(id)
                .build();
        return kafkaManager.updateBroker(params);
    }

    @PutMapping("/{id}/configs")
    public BrokerInfo putBrokerConfigs(
            @PathVariable("id") Integer id,
            @RequestBody BrokerConfigRequest request) {
        BrokerConfigUpdateParams params = BrokerConfigUpdateParams.builder()
                .brokerId(id)
                .config(request.getConfig())
                .build();
        return kafkaManager.updateBroker(params);
    }

}
