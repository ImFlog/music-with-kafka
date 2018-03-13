# :musical_note: music-with-kafka :speaker: :notes:
This repository contains material for the Devoxx 2018 university "De la musique collaborative avec Kafka".

## Prerequisites
1. You need to have a locally running Kafka broker on port 9092.
2. You need to set the environment variable $KAFKA_HOME (i.e : /usr/share/kafka).
3. You also need to find some .ogg files to play (more informations in the Streamer part).

## Kafka Connect
1. Create the twitter topic `$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic twitter_json --replication-factor 1`
2. Clone the following [repo](https://github.com/jcustenborder/kafka-connect-twitter)
3. Build the connector `mvn clean package`
4. Make it visible for Kafka connect `export CLASSPATH="$(find target/kafka-connect-target/usr/share/kafka-connect/kafka-connect-twitter/ -type f -name '*.jar' | tr '\n' ':')"` 
5. Change connect/twitter.properties to add your twitter tokens (use [twitter app manager](https://apps.twitter.com/))
6. Start kafka connect `$KAFKA_HOME/bin/connect-standalone $KAFKA_HOME/config/connect-standalone.properties connect/twitter.properties`

For testing purpose, you can run the loader_script located in /connect. It will inject fake tweets in you twitter_json topic and you will be able to run the rest of the code.

## Streamer

Streamer app needs a kafka server and a `test` topic.

Then, streamer app react to message of type `{"action":string, "name":string}` on topic `test`

Possible values for action: 
* `PLAY`
* `STOP`

Possible values for name:
* `drum[1-10]`
* `bass[1-10]`
* `sound[1-16]`

Example:

``` sh
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
> {"action": "PLAY", "name": "sound1"}
> {"action": "PLAY", "name": "drum4"}
> {"action": "PLAY", "name": "bass6"}
> {"action": "STOP", "name": "sound1"}
```

To start the streamer application :
`./gradlew build && ./gradlew bootRun`

(To complete)

## Client

Client app will listen for incoming message from streamer app by using SSE mechanism.
You need to provide ogg audio files in (client/audio) in order to play music.

The client needs a simple http server to be running.
You can use [live-server](https://www.npmjs.com/package/live-server)
`live-server .`

(To complete)
