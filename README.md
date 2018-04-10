# :musical_note: music-with-kafka :speaker: :notes:
This repository contains material for the Devoxx 2018 university "De la musique collaborative avec Kafka".

## Prerequisites
1. You need to have [docker](https://docs.docker.com/install/) and [docker-compose](https://docs.docker.com/compose/install/) installed.
2. You also need to find some .ogg files to play. Please use the following configuration : directories and files should be named the same way. i.e:
bass\
     bass0.ogg
     bass1.ogg
vocal\
     vocal0.ogg
     ... 
3. Launch Kafka and create the needed topics :
```
# Start Kafka
sudo docker-compose up -d kafka

# Create topics
sudo docker-compose exec kafka kafka-topics --create --topic twitter_json --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
sudo docker-compose exec kafka kafka-topics --create --topic users --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
sudo docker-compose exec kafka kafka-topics --create --topic user-feed --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
sudo docker-compose exec kafka kafka-topics --create --topic sounds --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
```

## Kafka Connect
We are using the [kafka-connect-twitter connector](https://github.com/jcustenborder/kafka-connect-twitter).

1. Change connect/twitter.json to add your twitter tokens (use [twitter app manager](https://apps.twitter.com/))
2. Launch the docker container with `sudo docker-compose up connect`
3. You can now interact with the REST API to manage your running connectors. i.e :
```
# Retrieve the available connectors
curl http://localhost:8082/connector-plugins | jq

# Retrieve the running connectors
curl http://localhost:8082/connectors | jq
```
4. Using curl you can now start the twitter connector with the following command:
`$ curl -X POST -H "Content-Type: application/json" --data-binary @connect/twitter.json localhost:8082/connectors`

Everything is ready ! You are now listening to #mwk on twitter !

For testing purpose, you can run the loader_script located in /connect. It will inject fake tweets in you twitter_json topic and you will be able to run the rest of the code.

## Kafka Streams
1. Launch the streaming application `streams/gradlew build && streams/gradlew bootRun`
2. You can check the messages are correctly written in your topic using `$KAFKA_HOME/bin/kafka-console-consumer.sh --topic sounds --bootstrap-server localhost:9092 --property "print.key=true`

## Emitter (SSE toward the frontend)
The Emitter is a simple bridge between the Client and Kafka Streams. 

It's consuming the 3 topics (sounds / user-feed / users) and simply redirects messages to the client via Server Sent Events (SSE)

To start the emitter application :
`./gradlew build && ./gradlew bootRun`

## Client
Client app will listen for incoming message from the emitter app by using SSE mechanism.
You need to provide ogg audio files in (client/audio) in order to play music. (See Prerequisites)

To start the client :
`yarn install && yarn start`

## KSQL
You will need a KSQL client.
Follow the [KSQL setup](https://github.com/confluentinc/ksql/tree/v0.5/docs/quickstart#setup) to do so. 

Then you can try the queries in ksql/queries.sql.
