package io.github.zm.kmpauth.core.platform.auth.driver

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.PlatformLoginIntent

expect class DefaultAuthDriver : AuthDriver {
    override suspend fun createLoginIntent(): PlatformLoginIntent

    override suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens>

    override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens>

    override suspend fun createLogoutIntent(): PlatformLoginIntent?

    override suspend fun getEndSessionRequestIntent(idToken: String): PlatformLoginIntent
}
