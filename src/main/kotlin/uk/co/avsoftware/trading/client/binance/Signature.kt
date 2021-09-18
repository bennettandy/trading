package uk.co.avsoftware.trading.client.binance

import org.springframework.stereotype.Component
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class Signature {
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun getSignature(data: String, key: String): String {
        var hmacSha256: ByteArray? = null
        hmacSha256 = try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), HMAC_SHA256)
            val mac = Mac.getInstance(HMAC_SHA256)
            mac.init(secretKeySpec)
            mac.doFinal(data.toByteArray())
        } catch (e: Exception) {
            throw RuntimeException("Failed to calculate hmac-sha256", e)
        }
        return hmacSha256?.toHex() ?: ""
    }

    companion object {
        private const val HMAC_SHA256 = "HmacSHA256"
    }
}