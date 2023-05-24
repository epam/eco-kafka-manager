package com.epam.eco.kafkamanager;

public interface TopicSearchCriteria extends SearchCriteria<TopicInfo> {
    boolean matches(TopicInfo obj);
}
