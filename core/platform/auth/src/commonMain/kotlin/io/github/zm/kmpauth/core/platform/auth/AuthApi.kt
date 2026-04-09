package io.github.zm.kmpauth.core.platform.auth

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.PlatformLoginIntent

interface AuthApi {
    suspend fun startLogin(): PlatformLoginIntent
    suspend fun completeLogin(payload: PlatformCallbackPayload): AuthTokens
    suspend fun refresh(refreshToken: String): AuthTokens
}
