package io.github.zm.kmpauth.core.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String? = null,
    val idToken: String? = null,
    val expiresAtEpochMs: Long? = null,
)
