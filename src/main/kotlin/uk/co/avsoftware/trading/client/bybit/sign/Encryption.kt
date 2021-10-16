package uk.co.avsoftware.trading.client.bybit.sign

import org.springframework.stereotype.Component
import kotlin.Throws
import java.security.NoSuchAlgorithmException
import java.util.TreeMap
import java.lang.StringBuilder
import javax.crypto.spec.SecretKeySpec
import java.lang.StringBuffer
import java.security.InvalidKeyException
import javax.crypto.Mac

@Component
class Encryption {
    /**
     *
     * @param params
     * Map<String></String>, String> params = new TreeMap<String></String>, String>(
     * new Comparator<String>() {
     * public int compare(String obj1, String obj2) {
     * //sort in alphabet order
     * return obj1.compareTo(obj2);
     * }
     * });
     * @param secret
     * @return
    </String> */
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun genQueryString(params: TreeMap<String, String>, secret: String): String {
        val keySet: Set<String> = params.keys
        val iter = keySet.iterator()
        val sb = StringBuilder()
        while (iter.hasNext()) {
            val key = iter.next()
            sb.append(key + "=" + params[key])
            sb.append("&")
        }
        sb.deleteCharAt(sb.length - 1)
        val sha256_HMAC = Mac.getInstance("HmacSHA256")
        val secret_key = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        sha256_HMAC.init(secret_key)
        return sb.toString() + "&sign=" + bytesToHex(sha256_HMAC.doFinal(sb.toString().toByteArray()))
    }

    companion object {
        private fun bytesToHex(hash: ByteArray): String {
            val hexString = StringBuffer()
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        }
    }
}