/*
 * Copyright 2020 EPAM Systems
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionSearchCriteria;
import com.epam.eco.kafkamanager.core.utils.PageUtils;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    public KafkaManager kafkaManager;

    @GetMapping
    public Page<TransactionInfo> getTransactionsPage(
            @RequestParam(value = "transactionId", required = false) String transactionId,
            @RequestParam(value = "topicName", required = false) String topicName,
            @RequestParam(value = "state", required = false) TransactionState state,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = PageUtils.buildPageableWithDefaultsIfNull(page, pageSize);
        TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
                .transactionalId(transactionId)
                .topicName(topicName)
                .state(state)
                .build();
        return kafkaManager.getTransactionPage(criteria, pageable);
    }

    @GetMapping("/{transactionId}")
    public TransactionInfo getTransaction(@PathVariable("transactionId") String transactionId) {
        return kafkaManager.getTransaction(transactionId);
    }

}
