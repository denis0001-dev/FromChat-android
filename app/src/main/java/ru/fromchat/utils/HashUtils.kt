package ru.fromchat.utils

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HashUtils {
    /**
     * Derive authentication secret using HKDF (HMAC-based Key Derivation Function)
     * Matches the frontend implementation in deriveAuthSecret
     * 
     * @param username The username
     * @param password The plain password
     * @return Base64-encoded 32-byte derived secret
     */
    fun deriveAuthSecret(username: String, password: String): String {
        val salt = "fromchat.user:$username".toByteArray(Charsets.UTF_8)
        val ikm = password.toByteArray(Charsets.UTF_8)
        val info = "auth-secret".toByteArray(Charsets.UTF_8)
        val length = 32
        
        val prk = hkdfExtract(salt, ikm)
        val okm = hkdfExpand(prk, info, length)
        
        return Base64.encodeToString(okm, Base64.NO_WRAP)
    }
    
    /**
     * HKDF Extract step: PRK = HMAC-Hash(salt, IKM)
     */
    private fun hkdfExtract(salt: ByteArray, ikm: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(salt, "HmacSHA256")
        mac.init(keySpec)
        return mac.doFinal(ikm)
    }
    
    /**
     * HKDF Expand step: OKM = HKDF-Expand(PRK, info, L)
     */
    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val blocks = mutableListOf<ByteArray>()
        var previous = ByteArray(0)
        var counter = 1
        
        while (blocks.sumOf { it.size } < length) {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(prk, "HmacSHA256")
            mac.init(keySpec)
            
            val input = previous + info + byteArrayOf(counter.toByte())
            previous = mac.doFinal(input)
            blocks.add(previous)
            counter++
        }
        
        val result = ByteArray(length)
        var offset = 0
        for (block in blocks) {
            val toCopy = minOf(block.size, length - offset)
            System.arraycopy(block, 0, result, offset, toCopy)
            offset += toCopy
            if (offset >= length) break
        }
        
        return result
    }
}

