package fr.ippon.streamer.controller

import com.fasterxml.jackson.core.type.TypeReference
import fr.ippon.streamer.domains.User
import fr.ippon.streamer.domains.Users
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class InteractiveQueries {

    val client: WebClient by lazy {
        WebClient.create("http://localhost:8080")
    }

    fun getTopTweetsByUsers(req: ServerRequest): Mono<ServerResponse> {
        val response: Mono<Map<String, Long>> = client
                .get()
                .uri("/tweets")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono()

        return ok().body(response)

    }

    fun getAllUsersInfo(req: ServerRequest): Mono<ServerResponse> {
        val response: Mono<List<User>> = client
                .get()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono()

        return ok().body(response.map { Users(it) })
    }

}