package fr.ippon.streamer

import com.fasterxml.jackson.databind.ObjectMapper
import fr.ippon.streamer.domains.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord

@Service
class ConsumerService(private val kafkaDataReceiver: KafkaReceiver<Int, String>, val mapper: ObjectMapper) {

    private final val stream: Flux<ReceiverRecord<Int, String>> by lazy {
        kafkaDataReceiver.receive().share()
    }

    fun streamMessage(): Flux<Payload> {
        return stream
                .doOnNext { record ->
                    record.receiverOffset().acknowledge()
                }
                .map { mapper.readValue(it.value(), Payload::class.java) }
    }


}