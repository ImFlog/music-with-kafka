# :musical_note: music-with-kafka :speaker: :notes:
This repository contains material for the Devoxx 2018 university "De la musique collaborative avec Kafka".

## Prerequisites
1. You need to have a locally running Kafka broker on port 9092.
2. You need to set the environment variable $KAFKA_HOME (i.e : /usr/share/kafka).
3. You also need to find some .ogg files to play. Please use the following configuration : directories and files should be named the same way. i.e:
bass\
     bass0.ogg
     bass1.ogg
vocal\
     vocal0.ogg
     ... 

## Starting Kafka
To simplify things, we simply added this [repo](https://github.com/wurstmeister/kafka-docker) and added what is needed for this project.

To start kafka go to `kafka-docker` and type : `docker-compose up -d`

Kafka will be available at localhost:9092.

## Kafka Connect
1. Do a `git submodule init && git submodule update`
2. Change connect/twitter.properties to add your twitter tokens (use [twitter app manager](https://apps.twitter.com/))
3. Launch the script `connect/start_connect.sh`

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
