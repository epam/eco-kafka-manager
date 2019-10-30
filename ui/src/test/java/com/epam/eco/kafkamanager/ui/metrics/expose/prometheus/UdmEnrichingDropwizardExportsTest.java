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
package com.epam.eco.kafkamanager.ui.metrics.expose.prometheus;

import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.kafkamanager.ui.metrics.expose.prometheus.UdmEnrichingDropwizardExports;

/**
 * @author Andrei_Tytsik
 */
public class UdmEnrichingDropwizardExportsTest {

    @Test
    public void testOriginMetricNameExtracted() throws Exception {
        String name = UdmEnrichingDropwizardExports.extractOriginMetricName("# HELP consumer_group_lag__eco_dom_datahub_epm_staf_integration_v7_m_applicant_0 Generated from Dropwizard metric import (metric=consumer_group_lag._eco_dom.datahub_epm_staf_integration_v7_m_applicant-0, type=com.epam.eco.kafkamanager.udmetrics.library.ConsumerGroupPartitionLagMetric)");
        Assert.assertEquals("consumer_group_lag._eco_dom.datahub_epm_staf_integration_v7_m_applicant-0", name);
    }

    @Test
    public void testNullMetricNameExtractedFromNonDropwizardHelp() throws Exception {
        String name = UdmEnrichingDropwizardExports.extractOriginMetricName("# TYPE consumer_group_lag__eco_dom_datahub_epm_staf_integration_v6_m_applicant_0 gauge");
        Assert.assertNull(name);
    }

}
