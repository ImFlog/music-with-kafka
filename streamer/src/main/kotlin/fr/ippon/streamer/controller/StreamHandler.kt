package fr.ippon.streamer.controller

import fr.ippon.streamer.domains.Payload
import fr.ippon.streamer.kafka.Consumer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

@Component
class StreamHandler(val kafkaConsumer: Consumer) {

    fun stream(req: ServerRequest): Mono<ServerResponse> = ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(kafkaConsumer.stream, Payload::class.java)

}