package com.epam.eco.kafkamanager.core.utils;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitUtil.class);

    private WaitUtil() {
    }

    public static void waitForCondition(
            Supplier<Boolean> condition,
            String conditionDescription
    ) {
        int maxAttempts = 10;
        int attempt = 0;
        long waitTimeMs = 200;

        while (attempt < maxAttempts) {
            try {
                if (Boolean.TRUE.equals(condition.get())) {
                    return;
                }
            } catch (Exception e) {
                LOGGER.warn("Error checking condition: {}", conditionDescription, e);
            }
            attempt++;
            ExceptionUtils.doQuietly(() -> Thread.sleep(waitTimeMs));
        }

        LOGGER.warn("Condition not met after {} attempts: {}", maxAttempts, conditionDescription);
    }
}
