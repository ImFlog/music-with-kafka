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

## Kafka Connect
1. Create the twitter topic `$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic twitter_json --replication-factor 1`
2. Clone the following [repo](https://github.com/jcustenborder/kafka-connect-twitter)
3. Build the connector `mvn clean package`
4. Make it visible for Kafka connect `export CLASSPATH="$(find target/kafka-connect-target/usr/share/kafka-connect/kafka-connect-twitter/ -type f -name '*.jar' | tr '\n' ':')"` 
5. Change connect/twitter.properties to add your twitter tokens (use [twitter app manager](https://apps.twitter.com/))
6. Start kafka connect `$KAFKA_HOME/bin/connect-standalone $KAFKA_HOME/config/connect-standalone.properties connect/twitter.properties`

For testing purpose, you can run the loader_script located in /connect. It will inject fake tweets in you twitter_json topic and you will be able to run the rest of the code.

## Kafka Streams
1. Create the topics
`$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic sounds --replication-factor 1
$$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic users --replication-factor 1`
2. Launch the streaming application `./gradlew build && ./gradlew bootRun`
3. You can check the messages are correctly written in your topic using `$KAFKA_HOME/bin/kafka-console-consumer.sh --topic sounds --bootstrap-server localhost:9092 --property "print.key=true`

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
