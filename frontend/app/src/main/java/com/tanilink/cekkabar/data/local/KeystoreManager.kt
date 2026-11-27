package com.tanilink.cekkabar.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Mengelola kunci enkripsi database SQLCipher menggunakan Android Keystore (AES-256-GCM).
 * Passphrase acak 32-byte di-generate sekali lalu dienkripsi dengan hardware-backed Keystore.
 */
class KeystoreManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateDatabasePassphrase(): ByteArray {
        val encryptedB64 = prefs.getString(KEY_ENC_PASSPHRASE, null)
        val ivB64 = prefs.getString(KEY_ENC_IV, null)

        if (encryptedB64 != null && ivB64 != null) {
            return decryptPassphrase(
                Base64.decode(encryptedB64, Base64.NO_WRAP),
                Base64.decode(ivB64, Base64.NO_WRAP)
            )
        }

        val rawPassphrase = ByteArray(32).apply {
            SecureRandom().nextBytes(this)
        }
        val (cipherText, iv) = encryptPassphrase(rawPassphrase)
        prefs.edit()
            .putString(KEY_ENC_PASSPHRASE, Base64.encodeToString(cipherText, Base64.NO_WRAP))
            .putString(KEY_ENC_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
        return rawPassphrase
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGen.init(spec)
        return keyGen.generateKey()
    }

    private fun encryptPassphrase(plain: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        return cipher.doFinal(plain) to cipher.iv
    }

    private fun decryptPassphrase(cipherText: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateMasterKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(cipherText)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "cekkabar_sqlcipher_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "cekkabar_secure_db_prefs"
        private const val KEY_ENC_PASSPHRASE = "enc_db_passphrase"
        private const val KEY_ENC_IV = "enc_db_iv"
    }
}

