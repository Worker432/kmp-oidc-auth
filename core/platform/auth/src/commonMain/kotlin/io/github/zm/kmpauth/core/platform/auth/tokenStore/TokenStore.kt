package io.github.zm.kmpauth.core.platform.auth.tokenStore

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.securestorage.SecureStorage

class TokenStore(
    private val storage: SecureStorage,
) {
    private companion object Keys {
        const val ACCESS = "auth.access"
        const val REFRESH = "auth.refresh"
        const val ID = "auth.id"
        const val EXPIRES_AT = "auth.expires_at"
    }

    suspend fun save(tokens: AuthTokens) {
        storage.putString(ACCESS, tokens.accessToken)

        tokens.refreshToken?.let { token ->
            storage.putString(REFRESH, token)
        } ?: storage.remove(REFRESH)

        tokens.idToken?.let { id ->
            storage.putString(ID, id)
        } ?: storage.remove(ID)

        tokens.expiresAtEpochMs?.let { expiresAt ->
            storage.putString(EXPIRES_AT, expiresAt.toString())
        } ?: storage.remove(EXPIRES_AT)
    }

    suspend fun loadAccessToken(): String? = storage.getString(ACCESS)

    suspend fun loadRefreshToken(): String? = storage.getString(REFRESH)

    suspend fun loadIdToken(): String? = storage.getString(ID)

    suspend fun loadExpiresAtEpochMs(): Long? = storage.getString(EXPIRES_AT)?.toLongOrNull()

    suspend fun clear() {
        storage.remove(ACCESS)
        storage.remove(REFRESH)
        storage.remove(ID)
        storage.remove(EXPIRES_AT)
    }
}
