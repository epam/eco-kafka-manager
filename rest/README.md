# Eco Kafka Manager REST

Eco Kafka Manager REST is a Spring Boot web application that exposes RESTful interface for interaction of third-party services and applications with the Kafka Manager.

## Minimum configuration file

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

## Running Eco Kafka Manager REST 

You can run Eco Kafka Manager: 

* As a standalone service 

* In Docker 

### Running as standalone

The prerequisites include:
* Java 8+
* [Apache Kafka](https://kafka.apache.org/)

Run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd /eco-kafka-manager/rest
mvn clean package
java -jar ./target/kafka-manager-rest-<version>.jar --spring.config.location=file://<path-to-config-file>
```

### Running in docker

The prerequisite includes [Docker](https://www.docker.com/get-started).

To build the image, run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd /eco-kafka-manager/rest
docker build -f ./Dockerfile -t epam/kafka-manager-rest:latest ./../
```

Run the container:
```
docker run --name kafka-manager-rest \
 --rm \
 -p 8086:8086 \
 -v <path-to-config-file>:/app/config/application.properties \
 epam/kafka-manager-rest:latest
```

To open Eco Kafka Manager REST API (swagger), go to [http://localhost:8086/swagger-ui.html#](http://localhost:8086/swagger-ui.html#)

### Note:

To reference files from the config file, it is possible to mount them with the
`-v` option. <br />
To tune JVM, use `-e 'JAVA_OPTS=<some JVM options>'`.
For example:
```
docker run --name kafka-manager-rest \
 --rm \
 -p 8086:8086 \
 -v <path-to-config-file>:/app/config/application.properties \
 -v <host-path-to-file-referenced-from-config>:<docker-path-to-file-referenced-from-config> \
 -m 3g \
 -e 'JAVA_OPTS=-Xms1g -Xmx1g' \
 epam/kafka-manager-rest:latest
```

## Configuration properties

Name | Description | Default
---  | ---         | --- 
`eco.kafkamanager.core.bootstrapServers` | A comma-separated list of Kafka brokers to connect to. |
`eco.kafkamanager.core.schemaRegistryUrl` | URL to the [Schema Registry](https://docs.confluent.io/current/schema-registry/index.html) REST API, used for reading records in Avro format serialized using the Schema Registry. |
`eco.kafkamanager.core.clientConfig[property]` | Common Kafka [client properties](https://kafka.apache.org/10/documentation.html#adminclientconfigs), used to connect to cluster. |
`eco.kafkamanager.core.metadataStoreBootstrapTimeoutInMs` | Max duration in milliseconds for bootstrapping user-defined metadata. If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started. | 180000
`eco.kafkamanager.core.transactionStoreBootstrapTimeoutInMs` | Max duration in milliseconds for bootstrapping transaction metadata (`__transaction_state`). If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started. | 180000
`eco.kafkamanager.core.transactionStoreBootstrapDataFreshness` | Defines the data freshness window for bootstrapping transaction metadata (`__transaction_state`). <br/><br/> Possible values: <br/> `ONE_HOUR` <br/> `TWO_HOURS` <br/> `THREE_HOURS` <br/> `ONE_DAY` <br/> `TWO_DAYS` <br/> `THREE_DAYS` <br/> `ONE_WEEK` <br/> `TWO_WEEKS` <br/> `THREE_WEEKS` | `ONE_HOUR`
`eco.kafkamanager.core.authz.kafka.enabled` | Controls whether authorization is enabled/disabled. | `false`
`eco.kafkamanager.core.authz.kafka.adminRoles` | List of admin roles. Users with this roles have all permissions. |
`eco.kafkamanager.core.authz.kafka.authorizerClass` | Kafka [Authorizer](https://cwiki.apache.org/confluence/display/KAFKA/KIP-11+-+Authorization+Interface) implementation. | `kafka.security.auth.SimpleAclAuthorizer`
`eco.kafkamanager.core.authz.kafka.authorizerConfig[property]` | Kafka [Authorizer](https://cwiki.apache.org/confluence/display/KAFKA/KIP-11+-+Authorization+Interface) properties. | 
`eco.kafkamanager.rest.asyncRequestTimeoutInMs` | Timeout in milliseconds for asynchronous request processing. | 300000

## License

Eco Kafka Manager REST is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
