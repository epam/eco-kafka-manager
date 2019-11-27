# Eco Kafka Manager

Eco Kafka Manager is a tool for monitoring and managing [Apache Kafka](https://kafka.apache.org/).

The currently supported version is [2.3](https://kafka.apache.org/23/documentation.html)

## Features

* Inspect Kafka resources (brokers, topics, consumers, permissions, transactions)
* Create/update/delete/purge topic
* Reset consumer group offsets
* Consumer group lag
* Add description to resources
* Export resources
* Topic/consumer offset RPM (Rate Per Minute)
* Topic browser to read messages in different formats
* Authorization
* REST API & client
* User-Defined metrics

![](km.gif)

## Project structure

The repository contains the following packages:
* [commons](/commons) - common models, utilities, extensions, etc
* [core](/core) - core services to manage Kafka resources
* [udmetrics](/udmetrics) - service to manage User-Defined metrics
* [rest](/rest) - RESTful interface
* [client](/client) - REST client
* [ui](/ui) - web UI

## Building artifacts
To build artifacts, run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd eco-kafka-manager
mvn clean package
```
To skip tests, JavaDocs, and static code analysis, run:
```
mvn clean package -PpackageOnly
```

## Quick start

The prerequisites for the quick start include:
* [Docker](https://www.docker.com/get-started)
* [Docker Compose](https://docs.docker.com/compose/install/)

### Installation to connect to an existing Apache Kafka

The installation consists of one service:
* Eco Kafka Manager UI

Note: Specify suitable value for `KAFKA_SERVERS_URL`.

For Linux, run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd eco-kafka-manager
export KAFKA_SERVERS_URL="kafka:9092"
docker-compose -f docker/docker-compose.yaml up
```

For Windows (Powershell), run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd eco-kafka-manager
$env:KAFKA_SERVERS_URL="kafka:9092"
docker-compose -f docker/docker-compose.yaml up
```

To open Eco Kafka Manager UI web interface, go to [http://localhost:8082](http://localhost:8082)

To stop all services, run:
```
docker-compose -f docker/docker-compose.yaml down
```

### All-in-one installation

The installation consists of:
* Zookeeper
* Kafka
* Eco Kafka Manager UI

Run the following command sequence:
```
git clone git@github.com:epam/eco-kafka-manager.git
cd eco-kafka-manager
docker-compose -f docker/docker-compose-all.yaml up
```

To open Eco Kafka Manager UI web interface, go to [http://localhost:8082](http://localhost:8082)

To stop all services, run:
```
docker-compose -f docker/docker-compose-all.yaml down
```

## Compatibility matrix

Eco Kafka Manager | Kafka
---  | --- 
1.3.x | 2.3.x
1.2.x | 2.2.x
1.1.x | 2.1.x
1.0.x | 2.0.x
0.1.x | 1.0.x

## License

Eco Kafka Manager is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
