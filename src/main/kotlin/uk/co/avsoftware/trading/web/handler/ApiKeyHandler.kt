package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.ApiKeyClient
import uk.co.avsoftware.trading.database.model.Person
import uk.co.avsoftware.trading.database.service.PersonService

@Component
class ApiKeyHandler(var apiKeyClient: ApiKeyClient, var personService: PersonService) {

    fun getApiKeyPermissions(): Mono<ServerResponse> =
        apiKeyClient.getApiKeyPermissions()
            .doOnEach {
                personService.savePersonDetails(Person("Fred Arse", 29, "Wigan"))
            }
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
}