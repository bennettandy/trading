package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.State

@Service
class StateService {

    private val dbFirestore by lazy { FirestoreClient.getFirestore()}

    companion object {
        private val logger = KotlinLogging.logger {}

        const val COL_NAME = "state"
    }

    fun retrieveState(): Mono<State> {

        val configCollection = dbFirestore.collection(COL_NAME)

        configCollection.document("root").get()

        val future: ApiFuture<DocumentSnapshot> = configCollection.document("root").get()

        return Mono.fromSupplier { future.get() }
            .doOnSuccess { logger.info { "Got configuration $it" } }
            .doOnError { logger.info { "Failed to get configuration ${it.message}" } }
            .map {  documentSnapshot -> documentSnapshot.toObject(State::class.java) }
    }

    fun updateState(state: State): Mono<String> {

        val configCollection = dbFirestore.collection(COL_NAME)

        val collectionsApiFuture: ApiFuture<WriteResult> = configCollection.document("root").set(state)

        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString() }
    }

}