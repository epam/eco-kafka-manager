# Eco Kafka Manager Core

Eco Kafka Manager Core is a Spring Boot auto-configuration library that provides core services to manage Kafka resources.

The library can be obtained from the Maven by adding the following dependency in the pom.xml:

```
<dependency>
    <groupId>com.epam.eco.kafkamanager</groupId>
    <artifactId>kafka-manager-core</artifactId>
    <version>${project.version}</version>
</dependency>

```

## Usage

To start using Eco Kafka Manager Core, add the corresponding jar on the classpath of your Spring Boot application, all the necessary beans are automatically created and wired to the application context.
```
@Autowired
private KafkaManager kafkaManager;

@Autowied
private KafkaAdminOperations adminOperations;
```

## Minimum configuration

**application.properties**
```
eco.kafkamanager.core.bootstrapServers=kafka:9092
```

or **application.yml**
```
eco:
    kafkamanager:
        core:
            bootstrapServers: kafka:9092
```

## License

Eco Kafka Manager Core is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
