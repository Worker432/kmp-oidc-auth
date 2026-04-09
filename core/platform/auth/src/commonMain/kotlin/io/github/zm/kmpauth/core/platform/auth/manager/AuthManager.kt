package io.github.zm.kmpauth.core.platform.auth.manager

import io.github.zm.kmpauth.core.platform.auth.model.AuthFlowResult
import io.github.zm.kmpauth.core.platform.auth.model.AuthState
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.PlatformLoginIntent
import kotlinx.coroutines.flow.StateFlow

interface AuthManager {
    val authState: StateFlow<AuthState>

    suspend fun getAuthState(): AuthState

    suspend fun startLogin(): AuthFlowResult

    suspend fun completeLogin(payload: PlatformCallbackPayload)

    suspend fun getAccessToken(): Result<String>

    suspend fun refreshTokens(): Result<String>

    suspend fun getEndSessionRequestIntent(): PlatformLoginIntent

    suspend fun completeLogout(payload: PlatformCallbackPayload): Result<Unit>
}
