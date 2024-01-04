/*******************************************************************************
 *  Copyright 2024 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.config.producer;

import com.epam.eco.kafkamanager.KafkaKmProducer;
import com.epam.eco.kafkamanager.ui.config.producer.KafkaKmUiProducer.KafkaProducerType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mikhail_Vershkov
 */
public class KafkaProducerResolver {

    private final KafkaKmUiProducer<?, ?> defaultProducer;
    private final Map<KafkaProducerType, KafkaKmUiProducer<?, ?>> producers;

    public KafkaProducerResolver(List<KafkaKmUiProducer<?, ?>> producers,
                                 KafkaKmUiProducer<?, ?> defaultProducer) {
        this.producers = producers.stream().collect(Collectors.toMap(KafkaKmUiProducer::getKafkaProducerType, Function.identity()));
        this.defaultProducer = defaultProducer;
    }

    public KafkaKmProducer<?,?> resolve(KafkaProducerType kafkaProducerType) {
        return producers.getOrDefault(kafkaProducerType, defaultProducer);
    }

}
