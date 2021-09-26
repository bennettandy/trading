package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.Configuration
import uk.co.avsoftware.trading.repository.service.ConfigurationService

@Service
class ConfigurationRepository(val configurationService: ConfigurationService) {

    fun getConfiguration(): Mono<Configuration> = configurationService.retrieveConfiguration()

    fun updateConfiguration(updatedConfig: Configuration?): Mono<Configuration> {
        return updatedConfig?.let {
            configurationService.updateConfiguration(it).map { updatedConfig }
        } ?: Mono.empty()
    }
}