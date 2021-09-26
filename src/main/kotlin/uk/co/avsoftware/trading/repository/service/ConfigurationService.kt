package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.Configuration
import uk.co.avsoftware.trading.database.model.Person

@Service
class ConfigurationService(val firestore: Firestore) {

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COL_NAME = "configuration"
    }

    fun saveConfiguration(configuration: Configuration): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()

        val collectionsApiFuture: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME)
            .document()
            .set(configuration)

        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString()}
    }

    fun retrieveConfiguration(): Mono<Configuration> {
        val query: Query = firestore.collection(COL_NAME).whereEqualTo("tradingState.symbol", "SOLBTC")

        val future: ApiFuture<QuerySnapshot> = query.get()

        return Mono.fromSupplier { future.get() }
            .doOnSuccess { logger.info { "Got configuration $it" } }
            .doOnError { logger.info { "Failed to get configuration ${it.message}" } }
            .map { it.documents }
            .map { it.first() }
            .map {  documentSnapshot -> documentSnapshot.toObject(Configuration::class.java) }
    }

    fun updatePersonDetails(person: Person): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()
        val collectionsApiFuture: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME).document(person.name).set(person);
        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString() }
    }

    fun deletePerson(name: String): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()
        val writeResult: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME).document(name).delete();

        return Mono.fromSupplier { writeResult.get() }
            .map { "Document with Patient ID $name has been deleted" }
    }
}