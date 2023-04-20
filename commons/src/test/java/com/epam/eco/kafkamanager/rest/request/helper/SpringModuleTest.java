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
package com.epam.eco.kafkamanager.rest.request.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.utils.TestEntityGenerator;
import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Raman_Babich
 */
public class SpringModuleTest {

    @Test
    public void testPageImplSerDe() throws IOException {
        int brokerAmount = 10;
        ObjectMapper objectMapper = TestObjectMapperSingleton.getObjectMapper();

        List<BrokerInfo> brokerInfoList = TestEntityGenerator.brokerInfoList(brokerAmount);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "1", Sort.NullHandling.NULLS_FIRST));
        orders.add(new Sort.Order(Sort.Direction.DESC, "2", Sort.NullHandling.NULLS_LAST));
        orders.add(new Sort.Order(Sort.Direction.ASC, "3", Sort.NullHandling.NATIVE));
        Pageable pageable = PageRequest.of(0, brokerAmount, Sort.by(orders));
        Page<BrokerInfo> page = new PageImpl<>(brokerInfoList, pageable, brokerAmount);

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(page);
        Page<BrokerInfo> resultPage = objectMapper.readValue(json, new TypeReference<PageImpl<BrokerInfo>>() {});

        Assertions.assertEquals(brokerInfoList, resultPage.getContent());
        Assertions.assertEquals(pageable, resultPage.getPageable());
    }

}
