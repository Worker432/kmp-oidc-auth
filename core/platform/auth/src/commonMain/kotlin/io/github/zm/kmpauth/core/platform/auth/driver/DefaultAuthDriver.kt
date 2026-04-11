package io.github.zm.kmpauth.core.platform.auth.driver

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload

expect class DefaultAuthDriver : AuthDriver {
    override suspend fun createLoginIntent(): PlatformAuthIntent

    override suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens>

    override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens>

    override suspend fun createLogoutIntent(): PlatformAuthIntent?

    override suspend fun getEndSessionRequestIntent(idToken: String): PlatformAuthIntent
}
