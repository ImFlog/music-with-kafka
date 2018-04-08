#!/bin/bash

# Clean previously created topics
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic twitter_json
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic sounds
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic user-feed
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic users


# Create all the required topics
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic twitter_json --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic sounds --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic user-feed --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic users --replication-factor 1

