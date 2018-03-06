package fr.ippon.streamer

import com.fasterxml.jackson.databind.ObjectMapper
import fr.ippon.streamer.domains.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver

@Service
class ConsumerService(private val kafkaDataReceiver: KafkaReceiver<Int, String>, private val mapper: ObjectMapper) {

    final val stream: Flux<Payload> by lazy {
        kafkaDataReceiver.receive()
                .doOnNext { it.receiverOffset().acknowledge() }
                .map { mapper.readValue(it.value(), Payload::class.java) }
                .share()
    }

}