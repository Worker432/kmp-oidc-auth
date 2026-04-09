package io.github.zm.kmpauth.core.platform.auth.driver

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.PlatformLoginIntent

interface AuthDriver {
    suspend fun createLoginIntent(): PlatformLoginIntent

    suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens>

    suspend fun refreshTokens(refreshToken: String): Result<AuthTokens>

    suspend fun createLogoutIntent(): PlatformLoginIntent?

    suspend fun getEndSessionRequestIntent(idToken: String): PlatformLoginIntent
}
