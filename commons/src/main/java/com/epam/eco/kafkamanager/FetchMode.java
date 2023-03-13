package com.epam.eco.kafkamanager;

import com.epam.eco.commons.kafka.helpers.BiDirectionalTopicRecordFetcher.FetchDirection;

public enum FetchMode {

        FETCH_FORWARD(FetchDirection.FORWARD),
        FETCH_BACKWARD(FetchDirection.BACKWARD),
        FETCH_RANGE(FetchDirection.FORWARD),
        FETCH_UNTIL_TIME(FetchDirection.BACKWARD),
        FETCH_FROM_TIME(FetchDirection.FORWARD);

        private final FetchDirection fetchDirection;

        FetchMode(FetchDirection fetchDirection) {
            this.fetchDirection = fetchDirection;
        }

        public FetchDirection getFetchDirection() {
            return this.fetchDirection;
        }
    }