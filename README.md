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
4. Launch the init_topic.sh script.

## Kafka Connect
1. Do a `git submodule init && git submodule update`
2. Change connect/twitter.properties to add your twitter tokens (use [twitter app manager](https://apps.twitter.com/))
3. Launch the script `connect/start_connect.sh`

For testing purpose, you can run the loader_script located in /connect. It will inject fake tweets in you twitter_json topic and you will be able to run the rest of the code.

## Kafka Streams
1. Launch the streaming application `streams/gradlew build && streams/gradlew bootRun`
2. You can check the messages are correctly written in your topic using `$KAFKA_HOME/bin/kafka-console-consumer.sh --topic sounds --bootstrap-server localhost:9092 --property "print.key=true`

## Emitter (SSE toward the frontend)
Streamer app react to message of type `{"action":string, "path":string}` on topic `sounds`

Possible values for action: 
* `PLAY`
* `STOP`

Possible values are the path of the files you want to play in the audio directory. For example :
* `drum/drum[1-10].ogg`
* `bass/bass[1-10].ogg`
* `sound/sound[1-16].ogg`

Example:

``` sh
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
> {"action": "PLAY", "path": "sound/sound1.ogg"}
> {"action": "PLAY", "path": "drum/drum4.ogg"}
> {"action": "PLAY", "path": "bass/bass6.ogg"}
> {"action": "STOP", "path": "sound/sound1.ogg"}
```

To start the emitter application :
`./gradlew build && ./gradlew bootRun`

## Client

Client app will listen for incoming message from streamer app by using SSE mechanism.
You need to provide ogg audio files in (client/audio) in order to play music.

To start the client :
`yarn install && yarn start`

(To complete)

## KSQL
You will need a KSQL client. Follow the [KSQL setup](https://github.com/confluentinc/ksql/tree/v0.5/docs/quickstart#setup) to do so.
Then you can try the queries in ksql/queries.sql.
