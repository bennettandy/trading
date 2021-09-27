package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.Position

@Service
class PositionService {

    private val dbFirestore by lazy { FirestoreClient.getFirestore()}

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COL_NAME = "positions"
    }


    // obtain open position
    fun retrieveOpenPosition(exchange: String = "binance", symbol: String): Mono<Position> {

        val positionsCollection = dbFirestore.collection(COL_NAME)

        val position = positionsCollection.document("${exchange}-${symbol}")

        val future: ApiFuture<DocumentSnapshot> = position.get()

        return Mono.fromSupplier { future.get() }
            .doOnSuccess { logger.info { "Got position $it" } }
            .doOnError { logger.info { "Failed to get position ${it.message}" } }
            .map { it.toObject(Position::class.java) }

    }

    fun createNewPosition(exchange: String = "binance", symbol: String, position: Position): Mono<Position> {

        val positionsCollection = dbFirestore.collection(COL_NAME)
        val future: ApiFuture<DocumentReference> = positionsCollection.add(position)

        return Mono.fromSupplier { future.get() }
            .doOnSuccess { logger.debug { "Created position $it" } }
            .doOnError { logger.debug { "Failed to create position ${it.message}" } }
            .doOnSuccess { logger.debug { "Document Ref id ${it.id}"}}
            .map { position }
    }

    fun updatePosition(position: Position): Mono<String> {
        val configCollection = dbFirestore.collection(COL_NAME)
        val collectionsApiFuture: ApiFuture<WriteResult> = configCollection.document(position.name).set(position)
        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString() }
    }

}