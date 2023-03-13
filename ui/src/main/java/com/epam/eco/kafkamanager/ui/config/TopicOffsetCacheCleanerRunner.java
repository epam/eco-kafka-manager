package com.epam.eco.kafkamanager.ui.config;

import com.epam.eco.commons.kafka.helpers.CachedTopicRecordFetcher;

/**
 * @author Mikhail_Vershkov
 */

public class TopicOffsetCacheCleanerRunner {

    private final long intervalMin;
    public TopicOffsetCacheCleanerRunner(long intervalMin) {
        this.intervalMin = intervalMin;
    }
    void init() {
        CachedTopicRecordFetcher.TopicCacheCleaner.with(intervalMin).run();
    }
}
