package io.github.zm.kmpauth.core.platform.auth.driver

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload

interface AuthDriver {
    suspend fun createLoginIntent(): PlatformAuthIntent

    suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens>

    suspend fun refreshTokens(refreshToken: String): Result<AuthTokens>

    suspend fun createLogoutIntent(): PlatformAuthIntent?

    suspend fun getEndSessionRequestIntent(idToken: String): PlatformAuthIntent
}
