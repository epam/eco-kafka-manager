# Eco Kafka Manager UDM

Eco Kafka Manager UDM is a Spring Boot auto-configuration library that provides service to manage User-Defined metrics (UDM). 

The library can be obtained from the Maven by adding the following dependency in the pom.xml:

```
<dependency>
    <groupId>com.epam.eco.kafkamanager</groupId>
    <artifactId>kafka-manager-udmetrics</artifactId>
    <version>${project.version}</version>
</dependency>

```

## Overview

User-Defined metrics are the metrics that allow to monitor Kafka resources like topics or consumer groups.

User-Defined metric types:
* `CONSUMER_GROUP_LAG` - calculates consumer group lag, separate metric is created for each topic-partition
* `TOPIC_OFFSET_INCREASE` - calculates topic offset increase, separate metric is created for each topic-partition

## Usage

To start using Eco Kafka Manager UDM, add the corresponding jar on the classpath of your Spring Boot application, all the necessary beans are automatically created and wired to the application context.

```
@Autowired
private UDMetricManager udMetricManager;
```

## License

Eco Kafka Manager UDM is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)