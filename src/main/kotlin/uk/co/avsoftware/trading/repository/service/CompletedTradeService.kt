package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.CompletedTrade

@Service
class CompletedTradeService {

    private val dbFirestore by lazy { FirestoreClient.getFirestore()}

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COL_NAME = "completed_trades"
    }

    fun saveNewCompletedTrade(completedTrade: CompletedTrade): Mono<String> {

        val apiFuture: ApiFuture<DocumentReference> = dbFirestore.collection(COL_NAME).add(completedTrade)

        return Mono.fromSupplier { apiFuture.get() }
            .doOnSuccess { logger.debug { "Created completed trade $it - id:${it.id}" } }
            .doOnError { logger.debug { "Failed to create completed trade ${it.message}" } }
            .doOnSuccess { logger.debug { "Document Ref id ${it.id}"}}
            .map { reference -> reference.id }
    }
}