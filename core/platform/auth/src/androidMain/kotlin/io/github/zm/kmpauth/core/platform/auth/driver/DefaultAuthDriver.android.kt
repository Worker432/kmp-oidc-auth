package io.github.zm.kmpauth.core.platform.auth.driver

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.OidcConfig
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.asAndroidIntent
import io.github.zm.kmpauth.core.platform.auth.model.platformLoginIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume

actual class DefaultAuthDriver(
    private val context: Context,
    private val config: OidcConfig,
) : AuthDriver {
    private val service = AuthorizationService(context.applicationContext)

    private suspend fun fetchConfig(issuer: String): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(issuer.toUri()) { cfg, ex ->
                if (cfg != null) {
                    cont.resume(cfg)
                } else {
                    cont.resumeWith(
                        Result.failure(
                            ex ?: IllegalStateException("OIDC discovery failed"),
                        ),
                    )
                }
            }
        }

    private fun getAuthIntent(request: AuthorizationRequest): Intent =
        service.getAuthorizationRequestIntent(request)

    private fun getAuthResponse(intent: Intent): AuthorizationResponse? =
        AuthorizationResponse.fromIntent(intent)

    private fun getAuthException(intent: Intent): AuthorizationException? =
        AuthorizationException.fromIntent(intent)

    private suspend fun performTokenRequest(
        request: TokenRequest,
    ): Pair<TokenResponse?, AuthorizationException?> =
        suspendCancellableCoroutine { cont ->
            service.performTokenRequest(request) { resp, ex ->
                cont.resume(resp to ex)
            }
        }

    private fun getEndSessionRequestIntent(request: EndSessionRequest): Intent =
        service.getEndSessionRequestIntent(request)

    private suspend fun discover(): AuthorizationServiceConfiguration {
        return fetchConfig(config.issuerUrl)
    }

    actual override suspend fun createLoginIntent(): PlatformAuthIntent {
        val serviceConfig = discover()

        val authRequest =
            AuthorizationRequest
                .Builder(
                    serviceConfig,
                    config.clientId,
                    ResponseTypeValues.CODE,
                    config.redirectUri.toUri(),
                ).setScope(config.scope)
                .build()

        val intent = getAuthIntent(authRequest)

        return platformLoginIntent(intent)
    }

    actual override suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens> {
        val intent =
            payload.asAndroidIntent()
                ?: return Result.failure(IllegalArgumentException("Callback intent is null"))

        val authException = getAuthException(intent)
        if (authException != null) return Result.failure(authException)

        val authResponse =
            getAuthResponse(intent)
                ?: return Result.failure(IllegalStateException("No AuthorizationResponse in callback intent"))

        val tokenRequest = authResponse.createTokenExchangeRequest()

        val (tokenResponse, tokenEx) = performTokenRequest(tokenRequest)
        if (tokenResponse == null) {
            return Result.failure(tokenEx ?: IllegalStateException("Token exchange failed"))
        }

        val access =
            tokenResponse.accessToken
                ?: return Result.failure(IllegalStateException("No access token"))

        return Result.success(
            AuthTokens(
                accessToken = access,
                refreshToken = tokenResponse.refreshToken,
                idToken = tokenResponse.idToken,
                expiresAtEpochMs = tokenResponse.accessTokenExpirationTime,
            ),
        )
    }

    actual override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens> {
        val serviceConfig = discover()
        val refreshRequest =
            TokenRequest.Builder(serviceConfig, config.clientId)
                .setGrantType("refresh_token")
                .setRefreshToken(refreshToken)
                .build()

        val (tokenResponse, tokenEx) = performTokenRequest(refreshRequest)
        if (tokenResponse == null) {
            return Result.failure(tokenEx ?: IllegalStateException("Refresh token request failed"))
        }

        val access = tokenResponse.accessToken
            ?: return Result.failure(IllegalStateException("No access token"))

        return Result.success(
            AuthTokens(
                accessToken = access,
                refreshToken = tokenResponse.refreshToken ?: refreshToken,
                idToken = tokenResponse.idToken,
                expiresAtEpochMs = tokenResponse.accessTokenExpirationTime,
            ),
        )
    }

    actual override suspend fun createLogoutIntent(): PlatformAuthIntent? = null

    actual override suspend fun getEndSessionRequestIntent(idToken: String): PlatformAuthIntent {
        val serviceConfig = discover()

        val extraParams = mutableMapOf<String, String>()

        val request = EndSessionRequest.Builder(serviceConfig)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(config.redirectUri.toUri())
            .setAdditionalParameters(extraParams)
            .build()

        return platformLoginIntent(getEndSessionRequestIntent(request))
    }
}
