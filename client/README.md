# Eco Kafka Manager Client

Eco Kafka Manager Client is a Spring Boot auto-configuration library that provides client for the Kafka Manager REST service.

The library can be obtained from the Maven by adding the following dependency in the pom.xml:

```
<dependency>
    <groupId>com.epam.eco.kafkamanager</groupId>
    <artifactId>kafka-manager-client</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Usage

To start using Eco Kafka Manager Client, add the corresponding jar on the classpath of your Spring Boot application, all the necessary beans are automatically created and wired to the application context.

```
@Autowired
private KafkaManager kafkaManager;
```

## Minimum configuration

**application.properties**
```
eco.kafkamanager.client.kafkaManagerUrl=http://kafka-manager-rest:8080
```

or **application.yml**
```
eco:
    kafkamanager:
        client:
            kafkaManagerUrl: http://kafka-manager-rest:8080
```

## Configuration properties

Name | Description | Default
---  | ---         | --- 
`eco.kafkamanager.client.kafkaManagerUrl` | Eco Kafka Manager REST Url | 

## License

Eco Kafka Manager Client is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)