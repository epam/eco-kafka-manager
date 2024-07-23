/*
 */
package com.epam.eco.kafkamanager.ui.topics.browser;

import org.springframework.http.ResponseEntity;

/**
 * @author Mikhail_Vershkov
 */
public interface KafkaRecordRepublisher {
    ResponseEntity<String> republish(String topicName,
                                     ReplacementType replacementType,
                                     String recordId,
                                     String headers,
                                     Integer timeout
    );
}
