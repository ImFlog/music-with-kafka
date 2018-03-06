package fr.ippon.streamer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Router(val streamHandler: StreamHandler) {

    @Bean
    fun routerFn() = router {
        GET("/stream").nest {
            accept(MediaType.TEXT_EVENT_STREAM, streamHandler::stream)
        }
    }

}