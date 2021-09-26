package uk.co.avsoftware.trading.repository.service


import com.google.cloud.secretmanager.v1.ProjectName
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import mu.KotlinLogging
import java.io.IOException


object SecretsService {

    private val logger = KotlinLogging.logger {}

    @Throws(IOException::class)
    fun listSecrets() {
        // TODO(developer): Replace these variables before running the sample.
        val projectId = "trading-326621"
        listSecrets(projectId)
    }

    // List all secrets for a project
    @Throws(IOException::class)
    fun listSecrets(projectId: String?) {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        SecretManagerServiceClient.create().use { client ->
            // Build the parent name.
            val projectName: ProjectName = ProjectName.of(projectId)

            // Get all secrets.
            val pagedResponse: SecretManagerServiceClient.ListSecretsPagedResponse = client.listSecrets(projectName)

            // List all secrets.
            pagedResponse
                .iterateAll()
                .forEach { secret -> logger.info {"Secret ${secret.name}" }}
        }
    }
}