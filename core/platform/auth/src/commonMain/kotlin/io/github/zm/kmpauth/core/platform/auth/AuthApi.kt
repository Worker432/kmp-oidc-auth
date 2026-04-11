package io.github.zm.kmpauth.core.platform.auth

import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload

interface AuthApi {
    suspend fun startLogin(): PlatformAuthIntent
    suspend fun completeLogin(payload: PlatformCallbackPayload): AuthTokens
    suspend fun refresh(refreshToken: String): AuthTokens
}
