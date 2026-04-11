package io.github.zm.kmpauth.core.platform.securestorage

expect class DefaultSecureStorage : SecureStorage {
    override suspend fun putString(
        key: String,
        value: String,
    )

    override suspend fun getString(key: String): String?

    override suspend fun remove(key: String)
}
