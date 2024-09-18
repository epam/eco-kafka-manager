# Eco Kafka Manager UI

Eco Kafka Manager UI is a Spring Boot web application that exposes UI interface for managing Kafka.

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

## Running Eco Kafka Manager UI

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
cd /eco-kafka-manager/ui
mvn clean package
java -jar ./target/kafka-manager-ui-<version>.jar --spring.config.location=file://<path-to-config-file>
```

### Running in docker

The prerequisite includes [Docker](https://www.docker.com/get-started).

To build the image, run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd /eco-kafka-manager/ui
docker build -f ./Dockerfile -t epam/eco-kafka-manager:latest ./../
```

Run the container:
```
docker run --name eco-kafka-manager \
 --rm \
 -p 8082:8082 \
 -v <path-to-config-file>:/app/config/application.properties \
 epam/eco-kafka-manager:latest
```

or using environment variables:
```
docker run --name eco-kafka-manager \
 --rm \
 -p 8085:8085 \
 -e SERVER_PORT=8085 \
 -e KAFKA_SERVERS_URL=kafka:9092 \
 -e SCHEMA_REGISTRY_URL=http://schema-registry \
 -e METADATA_BOOTSTRAP_TIMEOUT_MS=60000 \
 -e TX_BOOTSTRAP_TIMEOUT_MS=60000 \
 epam/eco-kafka-manager:latest
```

or using inline JSON configuration:
```
docker run --name eco-kafka-manager \
 --rm \
 -p 8082:8082 \
 -e SPRING_APPLICATION_JSON='{"eco":{"kafkamanager":{"core":{"bootstrapServers":"kafka:9092"}}}}' \
 epam/eco-kafka-manager:latest
```

To open Kafka Manager UI, go to [http://localhost:8082/](http://localhost:8082/)

#### Note:

To reference files from the config file, it is possible to mount them with the
`-v` option. <br />
To tune JVM, use `-e 'JAVA_OPTS=<some JVM options>'`.
For example:
```
docker run --name eco-kafka-manager \
 --rm \
 -p 8082:8082 \
 -v <path-to-config-file>:/app/config/application.properties \
 -v <host-path-to-file-referenced-from-config>:<docker-path-to-file-referenced-from-config> \
 -m 3g \
 -e 'JAVA_OPTS=-Xms1g -Xmx1g' \
 epam/eco-kafka-manager:latest
```

#### Dockerhub repo

[https://hub.docker.com/r/epam/eco-kafka-manager/tags](https://hub.docker.com/r/epam/eco-kafka-manager/tags)

## Configuration properties

Name | Environment Variable          | Description                                                                                                                                                                                                                                                                                     | Default                                   
---  |-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------
`sever.port` | SERVER_PORT                   | Server HTTP port.                                                                                                                                                                                                                                                                               | 8082                                      
`eco.kafkamanager.core.bootstrapServers` | KAFKA_SERVERS_URL             | A comma-separated list of Kafka brokers to connect to.                                                                                                                                                                                                                                          |
`eco.kafkamanager.core.zkConnect` | ZK_CONNECT                    | An external representation of Zookeeper connection string (with external host/IP) to connect to Kafka/ZK inside Docker containers. For other cases this property should be left empty.                                                                                                          |
`eco.kafkamanager.core.zkClientConfig[property]` |                               | Zookeeper client configuration properties. Can be used to enable TLS, see [mTLS authentication](https://docs.confluent.io/platform/current/security/zk-security.html#mtls-authentication)                                                                                                       |
`eco.kafkamanager.core.schemaRegistryUrl` | SCHEMA_REGISTRY_URL           | URL to the [Schema Registry](https://docs.confluent.io/current/schema-registry/index.html) REST API, used for reading records in Avro format serialized using the Schema Registry.                                                                                                              |
`eco.kafkamanager.core.clientConfig[property]` |                               | Common Kafka client [configuration properties](https://kafka.apache.org/documentation.html#adminclientconfigs), used to connect to cluster.                                                                                                                                                     |
`eco.kafkamanager.core.metadataStoreBootstrapTimeoutInMs` | METADATA_BOOTSTRAP_TIMEOUT_MS | Max duration in milliseconds for bootstrapping user-defined metadata. If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started.                                                                                                  | 180000                                    
`eco.kafkamanager.core.transactionStoreBootstrapTimeoutInMs` | TX_BOOTSTRAP_TIMEOUT_MS       | Max duration in milliseconds for bootstrapping transaction metadata (`__transaction_state`). If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started.                                                                           | 180000                                    
`eco.kafkamanager.core.transactionStoreBootstrapDataFreshness` | TX_BOOTSTRAP_DATA_FRESHNESS   | Defines the data freshness window for bootstrapping transaction metadata (`__transaction_state`). <br/><br/> Possible values: <br/> `ONE_HOUR` <br/> `TWO_HOURS` <br/> `THREE_HOURS` <br/> `ONE_DAY` <br/> `TWO_DAYS` <br/> `THREE_DAYS` <br/> `ONE_WEEK` <br/> `TWO_WEEKS` <br/> `THREE_WEEKS` | `ONE_HOUR`                                
`eco.kafkamanager.core.authz.kafka.enabled` |                               | Controls whether authorization at Kafka Manager level is enabled/disabled.                                                                                                                                                                                                                      | `false`                                   
`eco.kafkamanager.core.authz.kafka.adminRoles` |                               | List of admin roles. Users with this roles have all permissions.                                                                                                                                                                                                                                |
`eco.kafkamanager.core.authz.kafka.authorizerClass` |                               | Kafka [Authorizer](https://docs.confluent.io/platform/current/kafka/authorization.html#authorizer) class, used to authorize calls at Kafka Manager level.                                                                                                                                       | `kafka.security.authorizer.AclAuthorizer` 
`eco.kafkamanager.core.authz.kafka.authorizerConfig[property]` |                               | Kafka [Authorizer](https://docs.confluent.io/platform/current/kafka/authorization.html#authorizer) configuration properties.                                                                                                                                                                    |
`eco.kafkamanager.udmetrics.enabled` | UDM_ENABLED                   | Controls whether UDM manager is enabled/disabled.                                                                                                                                                                                                                                               | `false`                                   
`eco.kafkamanager.udmetrics.calculationIntervalInMs` | UDM_CALCULATION_INTERVAL      | Interval in milliseconds at which metric values are calculated/refreshed.                                                                                                                                                                                                                       | 60000                                     
`eco.kafkamanager.udmetrics.config.repo.kafka.bootstrapTimeoutInMs` | UDM_BOOTSTRAP_TIMEOUT_MS      | Max duration in milliseconds for bootstrapping metric configurations. If timeout is too small, you may observe stale data for some time (gets consistent eventually) after service is started.                                                                                                  | 60000                                     
`n/a` | SPRING_APPLICATION_JSON       | Flexible way to provide a set of configuration properties using inline JSON. For example, `eco.kafkamanager.core.schemaRegistryUrl` can be set as `{"eco":{"kafkamanager":{"store":{"schemaRegistryUrl":"http://schema-registry"}}}}`                                                           |
`eco.kafkamanager.ui.dataCatalogTool.urlTemplate` |                               | Data catalog tool link. This url pattern follow to external tool that describe topic. e.q. `https://datacatalog.epam.com/data/permanent-link/{topicname}` Expression {topicname} will be substituted by the actual topic name in a topic list.                                                  |
`eco.kafkamanager.ui.dataCatalogTool.name` |                               | External tool name that could describe a topic. Will be displayed in a topic list page, in appropriate column name.                                                                                                                                                                             |                                           |
`eco.kafkamanager.ui.schemaCatalogTool.urlTemplate` |                               | Schema catalog tool link. This url pattern follow to external tool that describe schema subject. e.q. `https://sandbox.datahub.epam.com/schema-catalog/schema/{schemaname}` Expression {schemaname} will be substituted by the actual subject name from every message.                          |
`eco.kafkamanager.ui.schemaCatalogTool.name` |                               | External tool name that could describe a schema subject.                                                                                                                                                                                                                                        |                                           |
`eco.kafkamanager.ui.showGridInTopicBrowser` |                               | If this parameter is set to true, then vertical lines will appear in topic browse                                                                                                                                                                                                               |                                           |
`eco.kafkamanager.ui.externalTools[X].name` |                               | This section adds name of the new external tool in a topics list page and External tools section in a topic info page                                                                                                                                                                           |                                           |
`eco.kafkamanager.ui.externalTools[X].urlTemplate` |                               | This section adds URL template of the new external tool in a topics list page and External tools section in a topic info page. E.q. https://sandbox.datahub.epam.com/external-tools/1/{topicname}                                                                                               |                                           |
`eco.kafkamanager.ui.externalTools[X].icon` |                               | This section adds icon representation of the new external tool in a topics list page and External tools section in a topic info page                                                                                                                                                            |                                           |
`eco.kafkamanager.ui.topicBrowser.tombstoneGeneratorReplacements[X].headerName` |                               | This section descrides replacement while tombstone generation. It is header name for substitution.                                                                                                                                                                                              |                                           |
`eco.kafkamanager.ui.topicBrowser.tombstoneGeneratorReplacements[X].replacement` |                               | This section descrides replacement while tombstone generation. It is expression for value for substitution.                                                                                                                                                                                     |                                           |
`eco.kafkamanager.ui.topicBrowser.replacementPatterns` |                               | It is patterns in html-like columns conntent to determine if it is valid html. In case of any of this patterns match column content, all special simbols like "<" or ">" will be replaced by "&lt;" og "&gt;" to pevent of future parsing as html code.                                         |                                           |
`eco.kafkamanager.ui.topicBrowser.filterByKeyPartition` |                               | If it is true, it force filtering by the key equals condition only by one partition. This partition calculated useing your own realization of com.epam.eco.kafkamanager.PartitionByKeyResolver.                                                                                                 | `false`                                   |
`eco.kafkamanager.ui.topicBrowser.timeFormat` |                               | Format of field that has logical type of `time-millis` or `time-micros`.                                                                                                                                                                                                                        | `HH:mm:ss.SSS`                            |
`eco.kafkamanager.ui.topicBrowser.dateFormat` |                               | Format of field that has logical type of `date`.                                                                                                                                                                                                                                                | `yyyy-MM-dd`                              |
`eco.kafkamanager.ui.topicBrowser.dateTimeFormat` |                               | Format of field that has logical type of `timestamp-millis` or `timestamp-micros`.                                                                                                                                                                                                              | `yyyy-MM-dd HH:mm:ss.SSS`                 |
`eco.kafkamanager.ui.topicBrowser.emptyIfNull` |                               | Representation format of a null values, if false it shows like `null`, otherwise just empty                                                                                                                                                                                                     | `true`                                    |


## Supporting AVRO logical types:

Kafka topic browser supports following logical types:
1. type: `bytes`, logicalType: `decimal` - values stores in a ByteBuffer type, then converts into a numeric value
2. type: `fixed`, size `17` - duration type, stored in a 3 byte value. 1 byte: count of month, 2 byte: count of days, 3 byte: count of milliseconds
3. type: `int`, logicalType: `date` - stores count of dates since 01.01.1970 UTC, converts in a date format according to a `eco.kafkamanager.ui.topicBrowser.dateFormat` value
4. type: `int`, logicalType: `time-millis` - stores a count of milliseconds since 01.01.1970 UTC, converts in a date format according to a `eco.kafkamanager.ui.topicBrowser.timeFormat` value.
5. type: `long`, logicalType: `time-micros` - stores a count of microseconds since 01.01.1970 UTC, converts in a date format according to a `eco.kafkamanager.ui.topicBrowser.timeFormat` value.
6. type: `long`, logicalType: `timestamp-millis` - stores a count of milliseconds since 01.01.1970 UTC, converts in a date format according to a `eco.kafkamanager.ui.topicBrowser.dateTimeFormat` value.
7. type: `long`, logicalType: `timestamp-micros` - stores a count of microseconds since 01.01.1970 UTC, converts in a date format according to a `eco.kafkamanager.ui.topicBrowser.dateTimeFormat` value.
8. type: `string`, logicalType: `uuid` - stores a UUID value as string.

## License

Eco Kafka Manager UI is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
