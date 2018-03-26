package fr.ippon.streamer.controller

import fr.ippon.streamer.kafka.Consumer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Mono

@Component
class StreamHandler(val kafkaConsumer: Consumer) {

    fun stream(req: ServerRequest): Mono<ServerResponse> = ok()
            .bodyToServerSentEvents(kafkaConsumer.stream)

    fun chartStream(req: ServerRequest): Mono<ServerResponse> = ok()
            .bodyToServerSentEvents(kafkaConsumer.chartsStream)

    fun userStream(req: ServerRequest): Mono<ServerResponse> = ok()
            .bodyToServerSentEvents(kafkaConsumer.usersStream)

}