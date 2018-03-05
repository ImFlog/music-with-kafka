# music-with-kafka

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

(To complete)