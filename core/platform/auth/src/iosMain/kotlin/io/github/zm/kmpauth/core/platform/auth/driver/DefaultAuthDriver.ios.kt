package io.github.zm.kmpauth.core.platform.auth.driver

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.PlatformLoginIntent

actual class DefaultAuthDriver : AuthDriver {
    actual override suspend fun createLoginIntent(): PlatformLoginIntent {
        TODO("Not yet implemented")
    }

    actual override suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens> {
        TODO("Not yet implemented")
    }

    actual override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens> {
        TODO("Not yet implemented")
    }

    actual override suspend fun createLogoutIntent(): PlatformLoginIntent? {
        TODO("Not yet implemented")
    }

    actual override suspend fun getEndSessionRequestIntent(idToken: String): PlatformLoginIntent {
        TODO("Not yet implemented")
    }
}