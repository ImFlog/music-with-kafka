#!/bin/bash

# Clean previously created topics
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --delete --topic twitter_json
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --delete --topic sounds
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --delete --topic user-feed
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --delete --topic users


# Create all the required topics
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --create --partitions 1 --topic twitter_json --replication-factor 1
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --create --partitions 1 --topic sounds --replication-factor 1
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --create --partitions 1 --topic user-feed --replication-factor 1
docker-compose exec kafka kafka-topics --zookeeper localhost:2181 --create --partitions 1 --topic users --replication-factor 1

