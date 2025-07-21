package com.oma.maksut.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.*
import android.util.Base64

class EncryptionUtils {
    companion object {
        private const val KEY_ALIAS = "maksut_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16

        @JvmStatic
        fun encrypt(data: String): String {
            try {
                val key = getOrCreateKey()
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val iv = generateIV()

                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))

                val encryptedData = cipher.doFinal(data.toByteArray())
                val combined = iv + encryptedData

                return Base64.encodeToString(combined, Base64.DEFAULT)
            } catch (e: Exception) {
                throw RuntimeException("Encryption failed", e)
            }
        }

        @JvmStatic
        fun decrypt(encryptedData: String): String {
            try {
                val key = getOrCreateKey()
                val cipher = Cipher.getInstance(TRANSFORMATION)

                val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
                val iv = decoded.sliceArray(0 until GCM_IV_LENGTH)
                val encrypted = decoded.sliceArray(GCM_IV_LENGTH until decoded.size)

                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))

                val decryptedData = cipher.doFinal(encrypted)
                return String(decryptedData)
            } catch (e: Exception) {
                throw RuntimeException("Decryption failed", e)
            }
        }

        @RequiresApi(23)
        private fun getOrCreateKey(): SecretKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (keyStore.containsAlias(KEY_ALIAS)) {
                return keyStore.getKey(KEY_ALIAS, null) as SecretKey
            }

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        private fun generateIV(): ByteArray {
            val iv = ByteArray(GCM_IV_LENGTH)
            Random().nextBytes(iv)
            return iv
        }
    }
}