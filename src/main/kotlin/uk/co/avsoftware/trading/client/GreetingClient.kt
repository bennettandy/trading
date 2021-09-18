package uk.co.avsoftware.trading.client

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.web.Greeting


@Component
class GreetingClient( builder: WebClient.Builder) {
    val client: WebClient by lazy { builder.baseUrl("http://localhost:8080").build() }

    fun getMessage(): Mono<String?>? {
        return client.get().uri("/hello").accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Greeting::class.java)
            .map(Greeting::message)
    }
}