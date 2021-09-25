package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.OrderResult
import uk.co.avsoftware.trading.database.model.Person
import uk.co.avsoftware.trading.database.model.Trade

@Service
class TradeService {
    companion object {
        const val COL_NAME = "trades"
    }

    // todo: temporary while I build trade object
    fun saveOrderResponse(orderResult: OrderResponse): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()

        val collectionsApiFuture: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME)
            .document(orderResult.orderId.toString()).set(orderResult)

        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString()}
    }

    fun saveTrade(person: Person): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()

        val collectionsApiFuture: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME)
            .document(person.name).set(person)

        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString()}
    }

    fun getPersonDetails(name: String): Mono<Person> {
        val dbFirestore = FirestoreClient.getFirestore()

        val documentReference: DocumentReference = dbFirestore.collection(COL_NAME).document(name)

        val future: ApiFuture<DocumentSnapshot> = documentReference.get()

        return Mono.fromSupplier { future.get() }
            .map { documentSnapshot -> documentSnapshot.toObject(Person::class.java) }
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