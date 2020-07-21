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
docker build -f ./Dockerfile -t epam/eco-kafka-manager-rest:latest ./../
```

Run the container:
```
docker run --name eco-kafka-manager-rest \
 --rm \
 -p 8086:8086 \
 -v <path-to-config-file>:/app/config/application.properties \
 epam/eco-kafka-manager-rest:latest
```

or using environment variables:
```
docker run --name eco-kafka-manager-rest \
 --rm \
 -p 8085:8085 \
 -e SERVER_PORT=8085 \
 -e KAFKA_SERVERS_URL=kafka:9092 \
 -e SCHEMA_REGISTRY_URL=http://schema-registry \
 -e METADATA_BOOTSTRAP_TIMEOUT_MS=60000 \
 -e TX_BOOTSTRAP_TIMEOUT_MS=60000 \
 -e ASYNC_REQUEST_TIMEOUT_MS=60000 \
 epam/eco-kafka-manager-rest:latest
```

or using inline JSON configuration:
```
docker run --name eco-kafka-manager-rest \
 --rm \
 -p 8082:8082 \
 -e SPRING_APPLICATION_JSON='{"eco":{"kafkamanager":{"core":{"bootstrapServers":"kafka:9092"}}}}' \
 epam/eco-kafka-manager-rest:latest
```

To open Eco Kafka Manager REST API (swagger), go to [http://localhost:8086/swagger-ui.html#](http://localhost:8086/swagger-ui.html#)

### Note:

To reference files from the config file, it is possible to mount them with the
`-v` option. <br />
To tune JVM, use `-e 'JAVA_OPTS=<some JVM options>'`.
For example:
```
docker run --name eco-kafka-manager-rest \
 --rm \
 -p 8086:8086 \
 -v <path-to-config-file>:/app/config/application.properties \
 -v <host-path-to-file-referenced-from-config>:<docker-path-to-file-referenced-from-config> \
 -m 3g \
 -e 'JAVA_OPTS=-Xms1g -Xmx1g' \
 epam/eco-kafka-manager-rest:latest
```

## Configuration properties

Name | Environment Variable | Description | Default
---  | ---                  | ---         | --- 
`sever.port` | SERVER_PORT | Server HTTP port. | 8086
`eco.kafkamanager.core.bootstrapServers` | KAFKA_SERVERS_URL | A comma-separated list of Kafka brokers to connect to. |
`eco.kafkamanager.core.zkConnect` | ZK_CONNECT | An external representation of Zookeeper connection string (with external host/IP) to connect to Kafka/ZK inside Docker containers. For other cases this property should be left empty. |
`eco.kafkamanager.core.schemaRegistryUrl` | SCHEMA_REGISTRY_URL | URL to the [Schema Registry](https://docs.confluent.io/current/schema-registry/index.html) REST API, used for reading records in Avro format serialized using the Schema Registry. |
`eco.kafkamanager.core.clientConfig[property]` | | Common Kafka [client properties](https://kafka.apache.org/23/documentation.html#adminclientconfigs), used to connect to cluster. |
`eco.kafkamanager.core.metadataStoreBootstrapTimeoutInMs` | METADATA_BOOTSTRAP_TIMEOUT_MS | Max duration in milliseconds for bootstrapping user-defined metadata. If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started. | 180000
`eco.kafkamanager.core.transactionStoreBootstrapTimeoutInMs` | TX_BOOTSTRAP_TIMEOUT_MS | Max duration in milliseconds for bootstrapping transaction metadata (`__transaction_state`). If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started. | 180000
`eco.kafkamanager.core.transactionStoreBootstrapDataFreshness` | TX_BOOTSTRAP_DATA_FRESHNESS | Defines the data freshness window for bootstrapping transaction metadata (`__transaction_state`). <br/><br/> Possible values: <br/> `ONE_HOUR` <br/> `TWO_HOURS` <br/> `THREE_HOURS` <br/> `ONE_DAY` <br/> `TWO_DAYS` <br/> `THREE_DAYS` <br/> `ONE_WEEK` <br/> `TWO_WEEKS` <br/> `THREE_WEEKS` | `ONE_HOUR`
`eco.kafkamanager.core.authz.kafka.enabled` | | Controls whether authorization is enabled/disabled. | `false`
`eco.kafkamanager.core.authz.kafka.adminRoles` | | List of admin roles. Users with this roles have all permissions. |
`eco.kafkamanager.core.authz.kafka.authorizerClass` | | Kafka [Authorizer](https://cwiki.apache.org/confluence/display/KAFKA/KIP-11+-+Authorization+Interface) implementation. | `kafka.security.auth.SimpleAclAuthorizer`
`eco.kafkamanager.core.authz.kafka.authorizerConfig[property]` | | Kafka [Authorizer](https://cwiki.apache.org/confluence/display/KAFKA/KIP-11+-+Authorization+Interface) properties. | 
`eco.kafkamanager.rest.asyncRequestTimeoutInMs` | ASYNC_REQUEST_TIMEOUT_MS | Timeout in milliseconds for asynchronous request processing. | 300000
`n/a` | SPRING_APPLICATION_JSON | Flexible way to provide a set of configuration properties using inline JSON. For example, `eco.kafkamanager.core.schemaRegistryUrl` can be set as `{"eco":{"kafkamanager":{"store":{"schemaRegistryUrl":"http://schema-registry"}}}}` | 

## License

Eco Kafka Manager REST is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
