# music-with-kafka

## Kafka Connect
1. Create the twitter topic `${KAFKA_PATH}/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --topic twitter_json --replication-factor 1`
2. Clone the following [repo](https://github.com/jcustenborder/kafka-connect-twitter)
3. Build the connector `mvn clean package`
4. Make it visible for Kafka connect `export CLASSPATH="$(find target/kafka-connect-target/usr/share/kafka-connect -type f -name '*.jar' | tr '\n' ':')"` 
5. Change connect/twitter.properties to add your twitter tokens
6. Start kafka connect `${KAFKA_PATH}/bin/connect-standalone /etc/kafka/connect-standalone.properties connect/twitter.properties`

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

(To complete)

## Client

Client app will listen for incoming message from streamer app by using SSE mechanism.
You need to provide ogg audio files in (client/audio) in order to play music.

(To complete)
