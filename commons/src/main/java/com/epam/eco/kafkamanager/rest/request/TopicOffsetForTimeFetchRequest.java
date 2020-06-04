package com.epam.eco.kafkamanager.rest.request;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopicOffsetForTimeFetchRequest {

    private final String topicName;
    private final Long timestamp;

    public TopicOffsetForTimeFetchRequest(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("timestamp") Long timestamp) {
        this.topicName = topicName;
        this.timestamp = timestamp;
    }

    public String getTopicName() {
        return topicName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicOffsetForTimeFetchRequest that = (TopicOffsetForTimeFetchRequest) o;
        return Objects.equals(topicName, that.topicName) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, timestamp);
    }

    @Override
    public String toString() {
        return "TopicOffsetForTimeFetchRequest{" +
                "topicName='" + topicName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
