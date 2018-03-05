package fr.ippon.streamer

import fr.ippon.streamer.domains.Payload
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class Controller(val kafkaConsumer: ConsumerService) {

    @GetMapping("/stream")
    @CrossOrigin("*")
    fun stream(): Flux<Payload> {
        return kafkaConsumer.streamMessage()
    }

}