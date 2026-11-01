package com.example.attentrack.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * "ID ar password save rakho" option. Password plain text e na, Android Keystore er key diye
 * AES-GCM encrypt kore rakha hoy. Logout korleo thake, jate abar login ek tap e hoy.
 */
class CredentialStore(context: Context) {

    private companion object {
        const val PREFS = "saved_login"
        const val KEY_REMEMBER = "remember"
        const val KEY_USER = "user_id"
        const val KEY_PASS = "password"
        const val ALIAS = "attendance_saved_login_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Checkbox er default (kokhono bondho kora na hole true). */
    fun isRememberEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER, true)

    fun save(userId: String, password: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
            val blob = cipher.iv + encrypted
            prefs.edit()
                .putBoolean(KEY_REMEMBER, true)
                .putString(KEY_USER, userId)
                .putString(KEY_PASS, Base64.encodeToString(blob, Base64.NO_WRAP))
                .apply()
        } catch (e: Exception) {
            // Keystore e somossya hole save korbo na, login thik-i cholbe
            forget()
        }
    }

    /** Saved (id, password) ba null. */
    fun load(): Pair<String, String>? {
        val user = prefs.getString(KEY_USER, null) ?: return null
        val blob = prefs.getString(KEY_PASS, null) ?: return null
        return try {
            val bytes = Base64.decode(blob, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, IV_SIZE)
            val data = bytes.copyOfRange(IV_SIZE, bytes.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            user to String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (e: Exception) {
            forget()
            null
        }
    }

    /** User save tick tule dile: shob muche jay ar checkbox off thake. */
    fun clear() {
        prefs.edit().putBoolean(KEY_REMEMBER, false).remove(KEY_USER).remove(KEY_PASS).apply()
    }

    /** Password change korle saved password o update hoye jay. */
    fun updatePasswordIfSaved(newPassword: String) {
        val user = prefs.getString(KEY_USER, null) ?: return
        save(user, newPassword)
    }

    private fun forget() {
        prefs.edit().remove(KEY_USER).remove(KEY_PASS).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }
}
