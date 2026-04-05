package io.github.zm.kmpauth.core.platform.securestorage

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.github.zm.kmpauth.core.domain.repository.SecureStorage

actual class DefaultSecureStorage(
    private val context: Context,
) : SecureStorage {
    private val prefs =
        EncryptedSharedPreferences
            .create(
                "auth_secure_prefs",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

    actual override suspend fun putString(
        key: String,
        value: String,
    ) {
        prefs.edit {
            putString(key, value)
        }
    }

    actual override suspend fun getString(key: String): String? = prefs.getString(key, null)

    actual override suspend fun remove(key: String) {
        prefs.edit {
            remove(key)
        }
    }
}
