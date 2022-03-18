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
package com.epam.eco.kafkamanager.ui.transactions;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionSearchCriteria;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TransactionController {

    public static final String TRANSACTIONS_VIEW = "transactions";
    public static final String TRANSACTION_VIEW = "transaction";

    public static final String ATTR_TRANSACTION = "transaction";
    public static final String ATTR_PAGE = "page";
    public static final String ATTR_SEARCH_CRITERIA = "searchCriteria";
    public static final String ATTR_TOTAL_COUNT = "totalCount";

    public static final String MAPPING_TRANSACTIONS = "/transactions";
    public static final String MAPPING_TRANSACTION = "/transactions/{id}";

    private static final int PAGE_SIZE = 10;

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value = MAPPING_TRANSACTIONS, method = RequestMethod.GET)
    public String transactions(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        TransactionSearchCriteria searchCriteria = TransactionSearchCriteria.fromJson(paramsMap);
        page = page != null && page > 0 ? page -1 : 0;

        Page<TransactionInfo> transactionPage = kafkaManager.getTransactionPage(
                searchCriteria,
                PageRequest.of(page, PAGE_SIZE));

        model.addAttribute(ATTR_SEARCH_CRITERIA, searchCriteria);
        model.addAttribute(ATTR_PAGE, wrap(transactionPage));
        model.addAttribute(ATTR_TOTAL_COUNT, kafkaManager.getTransactionCount());

        return TRANSACTIONS_VIEW;
    }

    @RequestMapping(value = MAPPING_TRANSACTION, method = RequestMethod.GET)
    public String transaction(@PathVariable("id") String transactionalId, Model model) {
        model.addAttribute(
                ATTR_TRANSACTION,
                TransactionInfoWrapper.wrap(kafkaManager.getTransaction(transactionalId)));
        return TRANSACTION_VIEW;
    }

    private static Page<TransactionInfoWrapper> wrap(Page<TransactionInfo> page) {
        return page.map(TransactionInfoWrapper::wrap);
    }

    public static String buildTransactionUrl(String transactionalId) {
        return MAPPING_TRANSACTION.replace("{id}", "" + transactionalId);
    }

}
