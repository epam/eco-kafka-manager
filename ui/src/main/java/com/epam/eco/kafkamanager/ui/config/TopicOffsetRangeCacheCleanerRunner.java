package com.epam.eco.kafkamanager.ui.config;

import com.epam.eco.commons.kafka.helpers.CachedTopicOffsetRangeFetcher;

/**
 * @author Mikhail_Vershkov
 */

public class TopicOffsetRangeCacheCleanerRunner {

    private final long intervalMin;
    public TopicOffsetRangeCacheCleanerRunner(long intervalMin) {
        this.intervalMin = intervalMin;
    }
    void init() {
        CachedTopicOffsetRangeFetcher.TopicOffsetRangeCacheCleaner.with(intervalMin);
    }
}
