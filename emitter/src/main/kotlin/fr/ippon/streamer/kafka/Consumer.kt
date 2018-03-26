package fr.ippon.streamer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import fr.ippon.streamer.domains.Action
import fr.ippon.streamer.domains.ChartPayload
import fr.ippon.streamer.domains.SoundsPayload
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver

@Service
class Consumer(
        @Qualifier("soundsReceiver") private val soundsReceiver: KafkaReceiver<String, String>,
        @Qualifier("chartsReceiver") private val chartsReceiver: KafkaReceiver<String, String>,
        @Qualifier("usersReceiver") private val usersReceiver: KafkaReceiver<String, String>,
        private val mapper: ObjectMapper) {

    val stream: Flux<SoundsPayload> by lazy {
        soundsReceiver.receive()
                .doOnNext { it.receiverOffset().acknowledge() }
                .map {
                    tryOr(SoundsPayload(listOf())) {
                        mapper.readValue(it.value(), SoundsPayload::class.java)
                    }
                }
                .share()
    }

    val chartsStream: Flux<ChartPayload> by lazy {
        chartsReceiver.receive()
                .doOnNext { it.receiverOffset().acknowledge() }
                .map {
                    tryOr(ChartPayload(listOf())) {
                        mapper.readValue(it.value(), ChartPayload::class.java)
                    }
                }
                .share()
    }

    val usersStream: Flux<String> by lazy {
        usersReceiver
                .receive()
                .doOnNext { it.receiverOffset().acknowledge() }
                .doOnNext { println("user = ${it.value()}") }
                .map {
                    tryOr("") {
                        it.value()
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