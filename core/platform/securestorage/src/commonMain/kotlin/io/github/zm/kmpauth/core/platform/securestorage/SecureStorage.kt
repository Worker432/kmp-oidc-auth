package io.github.zm.kmpauth.core.platform.securestorage

interface SecureStorage {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?

    suspend fun remove(key: String)
}
