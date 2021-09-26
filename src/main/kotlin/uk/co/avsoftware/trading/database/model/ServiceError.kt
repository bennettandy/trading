package uk.co.avsoftware.trading.database.model

data class ServiceError(
    val message: String
){
    companion object {
        private const val UNEXPECTED_ERROR = "unexpected error"
        fun from( t: Throwable) = ServiceError(t.message ?: UNEXPECTED_ERROR)
    }
}
