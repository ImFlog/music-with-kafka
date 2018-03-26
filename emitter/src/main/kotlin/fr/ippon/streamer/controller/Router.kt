package fr.ippon.streamer.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Router(val streamHandler: StreamHandler, val interactiveQueries: InteractiveQueries) {

    @Bean
    fun routerFn() = router {
        "/stream".nest {
            GET("/sounds").nest {
                accept(MediaType.TEXT_EVENT_STREAM, streamHandler::stream)
            }
            GET("/charts").nest {
                accept(MediaType.TEXT_EVENT_STREAM, streamHandler::chartStream)
            }
            GET("/users").nest {
                accept(MediaType.TEXT_EVENT_STREAM, streamHandler::userStream)

            }
        }
        GET("/tweets", interactiveQueries::getTopTweetsByUsers)
        GET("/users", interactiveQueries::getAllUsersInfo)
    }

}