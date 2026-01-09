package com.epam.eco.kafkamanager.core.utils;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {
    private static final Logger log = LoggerFactory.getLogger(ExceptionUtils.class);

    public static <T> T doQuietly(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.warn("extinguish exception: ", e);
        }
        return null;
    }

    public static void doQuietly(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("extinguish exception: ", e);
        }
    }

    public static <T> T unchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unchecked(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    private ExceptionUtils() {}
}
