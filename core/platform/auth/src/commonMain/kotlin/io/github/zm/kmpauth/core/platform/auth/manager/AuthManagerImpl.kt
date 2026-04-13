package io.github.zm.kmpauth.core.platform.auth.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.github.zm.kmpauth.core.platform.auth.driver.AuthDriver
import io.github.zm.kmpauth.core.platform.auth.model.AuthFlowResult
import io.github.zm.kmpauth.core.platform.auth.model.AuthState
import io.github.zm.kmpauth.core.platform.auth.model.OidcConfig
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.isValid
import io.github.zm.kmpauth.core.platform.auth.tokenStore.TokenStore
import kotlin.time.Clock

class AuthManagerImpl(
    private val driver: AuthDriver,
    private val tokenStore: TokenStore,
    private val config: OidcConfig
) : AuthManager {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null
    private var cachedExpiresAtEpochMs: Long? = null

    private val mScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun isAccessTokenValid(nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()): Boolean {
        val accessToken = cachedAccessToken
        if (accessToken.isNullOrBlank()) return false

        val expiresAt = cachedExpiresAtEpochMs
        return expiresAt == null || expiresAt > nowEpochMs
    }

    init {
        mScope.launch {
            cachedAccessToken = tokenStore.loadAccessToken()
            cachedRefreshToken = tokenStore.loadRefreshToken()
            cachedExpiresAtEpochMs = tokenStore.loadExpiresAtEpochMs()
            _authState.value =
                if (isAccessTokenValid()) AuthState.Authorized else AuthState.Unauthorized
        }
    }

    override suspend fun getAuthState(): AuthState {
        if (!config.isValid()) {
            return AuthState.ConfigInvalid
        }

        val accessToken = tokenStore.loadAccessToken()
        val refreshToken = tokenStore.loadRefreshToken()
        val expiresAtEpochMs = tokenStore.loadExpiresAtEpochMs()

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            return AuthState.Unauthorized
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val needRefresh = expiresAtEpochMs != null && expiresAtEpochMs <= now

        return if (needRefresh) {
            AuthState.NeedRefresh
        } else {
            AuthState.Authorized
        }
    }

    override suspend fun startLogin(): AuthFlowResult {
        if (isAccessTokenValid()) return AuthFlowResult.AlreadyAuthorized

        return runCatching {
            val intent = driver.createLoginIntent()
            AuthFlowResult.Launch(intent)
        }.getOrElse { e ->
            AuthFlowResult.Error(
                "Failed to start login",
                e,
            )
        }
    }

    override suspend fun completeLogin(payload: PlatformCallbackPayload): Result<Unit> =
        driver
            .exchangeTokens(payload)
            .onSuccess { tokens ->
                tokenStore.save(tokens)

                cachedAccessToken = tokens.accessToken
                cachedRefreshToken = tokens.refreshToken
                cachedExpiresAtEpochMs = tokens.expiresAtEpochMs

                _authState.value =
                    if (isAccessTokenValid()) AuthState.Authorized else AuthState.Unauthorized
            }.map { }

    override suspend fun getAccessToken(): Result<String> {
        return if (isAccessTokenValid()) {
            Result.success(cachedAccessToken!!)
        } else {
            Result.failure(IllegalStateException("No valid access token"))
        }
    }


    override suspend fun refreshTokens(): Result<String> {
        val refreshToken = cachedRefreshToken ?: tokenStore.loadRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            _authState.value = AuthState.Unauthorized
            return Result.failure(IllegalStateException("No refresh token"))
        }

        return driver.refreshTokens(refreshToken)
            .onSuccess { tokens ->
                tokenStore.save(tokens)
                cachedAccessToken = tokens.accessToken
                cachedRefreshToken = tokens.refreshToken
                cachedExpiresAtEpochMs = tokens.expiresAtEpochMs
                _authState.value = AuthState.Authorized
            }
            .onFailure {
                tokenStore.clear()
                cachedAccessToken = null
                cachedRefreshToken = null
                cachedExpiresAtEpochMs = null
                _authState.value = AuthState.Unauthorized
            }
            .map { it.accessToken }
    }

    override suspend fun getEndSessionRequestIntent(): PlatformAuthIntent {
        val idToken = tokenStore.loadIdToken()
            ?: throw RuntimeException("Не найден id токен")

        return driver.getEndSessionRequestIntent(idToken)
    }

    override suspend fun completeLogout(payload: PlatformCallbackPayload): Result<Unit> {
        tokenStore.clear()
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedExpiresAtEpochMs = null
        _authState.value = AuthState.Unauthorized

        return Result.success(Unit)
    }
}
