package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.OpenTrade
import uk.co.avsoftware.trading.database.model.State

@Service
class OpenTradeService {

    private val dbFirestore by lazy { FirestoreClient.getFirestore()}

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COL_NAME = "trades"
    }

    fun retrieveOpenTrade(documentReference: DocumentReference): Mono<OpenTrade> {
        val configCollection = dbFirestore.collection(COL_NAME)
        val future: ApiFuture<DocumentSnapshot> = configCollection.document(documentReference.id).get()
        return Mono.fromSupplier { future.get() }
            .doOnSuccess { logger.info { "Got state $it" } }
            .doOnError { logger.info { "Failed to get state ${it.message}" } }
            .map {  documentSnapshot -> documentSnapshot.toObject(OpenTrade::class.java) }
    }

    fun saveNewOpenTrade(openTrade: OpenTrade): Mono<DocumentReference> {
        val collection = dbFirestore.collection(COL_NAME)
        val apiFuture: ApiFuture<DocumentReference> = collection.add(openTrade)
        return Mono.fromSupplier { apiFuture.get() }
            .doOnSuccess { logger.debug { "Created open trade $it - id:${it.id}" } }
            .doOnError { logger.debug { "Failed to create completed trade ${it.message}" } }
            .doOnSuccess { logger.debug { "Document Ref id ${it.id}"}}
            .flatMap { reference -> Mono.fromSupplier { collection.document(reference.id) } }
    }

    fun updateOpenTrade(openTrade: OpenTrade, documentId: String): Mono<OpenTrade> {
        val configCollection = dbFirestore.collection(COL_NAME)
        val collectionsApiFuture: ApiFuture<WriteResult> = configCollection.document(documentId).set(openTrade)
        return Mono.fromSupplier { collectionsApiFuture.get() }
            .checkpoint("update State")
            .doOnSuccess { logger.info { "Updated State: $it" }}
            .doOnError { logger.info { "Failed to update state ${it.message}" } }
            .thenReturn(openTrade)
    }
}