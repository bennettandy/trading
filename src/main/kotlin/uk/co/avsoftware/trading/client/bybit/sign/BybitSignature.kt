package uk.co.avsoftware.trading.client.bybit.sign

import org.springframework.stereotype.Component
import uk.co.avsoftware.trading.client.binance.config.BinanceConfigProperties
import uk.co.avsoftware.trading.client.binance.sign.BybitSigner
import uk.co.avsoftware.trading.client.bybit.config.BybitConfigProperties
import java.time.Clock
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class BybitSignature(val bybitConfigProperties: BybitConfigProperties, encryption: Encryption ) : BybitSigner {
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun getSignature(data: String, key: String): String {
        val hmacSha256: ByteArray? = try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), HMAC_SHA256)
            val mac = Mac.getInstance(HMAC_SHA256)
            mac.init(secretKeySpec)
            mac.doFinal(data.toByteArray())
        } catch (e: Exception) {
            throw RuntimeException("Failed to calculate hmac-sha256", e)
        }
        return hmacSha256?.toHex() ?: ""
    }

    override fun getApiKey(): String = bybitConfigProperties.key

    override fun signQueryString(queryString: String): String =
        "${queryString}&sign=${sign(queryString)}"

    private fun sign(message: String): String = getSignature(message, bybitConfigProperties.secret)

    companion object {
        private const val HMAC_SHA256 = "HmacSHA256"
    }
}