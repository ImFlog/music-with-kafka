package fr.ippon.streamer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import fr.ippon.streamer.domains.Action
import fr.ippon.streamer.domains.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver

@Service
class Consumer(private val kafkaDataReceiver: KafkaReceiver<String, String>, private val mapper: ObjectMapper) {

    final val stream: Flux<Payload> by lazy {
        kafkaDataReceiver.receive()
                .doOnNext { it.receiverOffset().acknowledge() }
                .map {
                    tryOr(Payload(Action.NOTHING, listOf())) {
                        mapper.readValue(it.value(), Payload::class.java)
                    }
                }
                .share()
    }

    private fun <T> tryOr(defaultValue: T, f: () -> T): T {
        return try {
            f()
        } catch (e: Exception) {
            defaultValue
        }
    }


}