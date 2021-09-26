package uk.co.avsoftware.trading.database

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import javax.annotation.PostConstruct


@Service
class FirebaseInitialise {

    private val logger = KotlinLogging.logger {}

    @Value("\${sm://firebase-service-account}")
    protected lateinit var firebaseConfig: String

    @PostConstruct
    fun initialize() {
        logger.info { "Initialising Firebase" }
        try {
            val inputStream: InputStream = firebaseConfig.byteInputStream()
            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build()
            FirebaseApp.initializeApp(options)
            logger.info { "Initialised Firebase" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialise Firebase" }
        }
    }

}