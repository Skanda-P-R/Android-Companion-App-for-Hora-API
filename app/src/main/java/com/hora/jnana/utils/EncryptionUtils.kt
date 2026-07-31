package com.hora.jnana.utils

import android.util.Base64
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object EncryptionUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    // Obfuscated static key for shareability between apps/devices.
    // This is derived from bytes to avoid simple string searching in decompiled code.
    private val KEY_BYTES = byteArrayOf(
        0x48, 0x6f, 0x72, 0x61, 0x43, 0x6f, 0x6d, 0x70,
        0x61, 0x6e, 0x69, 0x6f, 0x6e, 0x4b, 0x65, 0x79,
        0x31, 0x32, 0x33, 0x5f, 0x53, 0x65, 0x63, 0x72,
        0x65, 0x74, 0x5f, 0x4b, 0x65, 0x79, 0x5f, 0x21
    ).copyOf(32) // 256-bit key

    private fun getSecretKey() = SecretKeySpec(KEY_BYTES, "AES")

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec)
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // Prepend IV to the encrypted data so it can be extracted during decryption
        val byteBuffer = ByteBuffer.allocate(iv.size + encryptedData.size)
        byteBuffer.put(iv)
        byteBuffer.put(encryptedData)
        
        return Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT)
    }

    fun decrypt(encryptedDataWithIv: String): String {
        val decoded = try {
            Base64.decode(encryptedDataWithIv, Base64.DEFAULT)
        } catch (_: Exception) {
            throw Exception("Invalid encoded data")
        }
        
        if (decoded.size < IV_LENGTH_BYTE) throw Exception("Data too short")
        
        val byteBuffer = ByteBuffer.wrap(decoded)
        
        val iv = ByteArray(IV_LENGTH_BYTE)
        byteBuffer.get(iv)
        
        val encryptedData = ByteArray(byteBuffer.remaining())
        byteBuffer.get(encryptedData)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decryptedData = cipher.doFinal(encryptedData)
        
        return String(decryptedData)
    }
}
