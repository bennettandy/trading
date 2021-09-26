package uk.co.avsoftware.trading.database

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import javax.annotation.PostConstruct


@Service
class FirebaseInitialise {

    private val logger = KotlinLogging.logger {}

    @PostConstruct
    fun initialize() {
        logger.info { "Initialising Firebase" }
        try {
            if (File("./serviceaccount.json").exists()){
                logger.info{"CONFIG EXISTS"}
            }

            val serviceAccount = FileInputStream("./serviceaccount.json")

            val options: FirebaseOptions = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialise Firebase" }
        }
    }

    @Bean
    fun provideFirestore(): Firestore = FirestoreClient.getFirestore()
}