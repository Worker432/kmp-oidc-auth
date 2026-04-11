@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.driver

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationService
import cocoapods.AppAuth.OIDServiceConfiguration
import cocoapods.AppAuth.OIDEndSessionRequest
import cocoapods.AppAuth.OIDGrantTypeRefreshToken
import cocoapods.AppAuth.OIDResponseTypeCode
import cocoapods.AppAuth.OIDTokenRequest
import cocoapods.AppAuth.OIDTokenResponse
import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.OidcConfig
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.asIosAuthResponse
import io.github.zm.kmpauth.core.platform.auth.model.asIosErrorDescription
import io.github.zm.kmpauth.core.platform.auth.model.platformEndSessionIntent
import io.github.zm.kmpauth.core.platform.auth.model.platformLoginIntent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class DefaultAuthDriver(
    private val config: OidcConfig,
) : AuthDriver {
    private suspend fun fetchConfig(issuer: NSURL): OIDServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            OIDAuthorizationService.discoverServiceConfigurationForIssuer(
                issuerURL = issuer,
            ) { cfg, error ->
                if (cfg != null) {
                    cont.resume(cfg)
                } else {
                    cont.resumeWithException(
                        IllegalStateException(
                            error?.localizedDescription ?: "OIDC discovery failed",
                        ),
                    )
                }
            }
        }

    private fun buildAuthRequest(
        config: OIDServiceConfiguration,
        clientId: String,
        redirectUri: String,
        scope: String,
    ): OIDAuthorizationRequest =
        OIDAuthorizationRequest(
            configuration = config,
            clientId = clientId,
            clientSecret = null,
            scopes = scope.split(" "),
            redirectURL = NSURL(string = redirectUri),
            responseType = OIDResponseTypeCode ?: "Empty response type",
            additionalParameters = null,
        )

    private suspend fun performTokenRequest(request: OIDTokenRequest): OIDTokenResponse =
        suspendCancellableCoroutine { cont ->
            OIDAuthorizationService.performTokenRequest(request) { resp, error ->
                if (resp != null) {
                    cont.resume(resp)
                } else {
                    cont.resumeWithException(
                        IllegalStateException(
                            error?.localizedDescription ?: "Token request failed",
                        ),
                    )
                }
            }
        }

    private suspend fun discover(): OIDServiceConfiguration {
        val issuer =
            NSURL(string = config.issuerUrl)
                ?: error("Bad issuerUrl: ${config.issuerUrl}")
        return fetchConfig(issuer)
    }

    actual override suspend fun createLoginIntent(): PlatformAuthIntent {
        val serviceConfig = discover()

        val request =
            buildAuthRequest(
                config = serviceConfig,
                clientId = config.clientId,
                redirectUri = config.redirectUri,
                scope = config.scope,
            )

        return platformLoginIntent(request)
    }

    actual override suspend fun exchangeTokens(payload: PlatformCallbackPayload): Result<AuthTokens> =
        runCatching {
            payload.asIosErrorDescription()?.let { msg ->
                throw IllegalStateException(msg)
            }

            val resp =
                payload.asIosAuthResponse()
                    ?: throw IllegalStateException("No authorization response")

            val tokenReq =
                resp.tokenExchangeRequest()
                    ?: throw IllegalStateException("No token exchange request")

            val tokenResp = performTokenRequest(tokenReq)

            val access =
                tokenResp.accessToken
                    ?: throw IllegalStateException("No access token")

            val expiresAtEpochMs =
                tokenResp.accessTokenExpirationDate
                    ?.timeIntervalSince1970
                    ?.let { (it * 1000).toLong() }

            AuthTokens(
                accessToken = access,
                refreshToken = tokenResp.refreshToken,
                idToken = tokenResp.idToken,
                expiresAtEpochMs = expiresAtEpochMs,
            )
        }

    actual override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens> =
        runCatching {
            val serviceConfig = discover()
            val tokenRequest =
                OIDTokenRequest(
                    configuration = serviceConfig,
                    grantType = OIDGrantTypeRefreshToken ?: "refresh_token",
                    authorizationCode = null,
                    redirectURL = null,
                    clientID = config.clientId,
                    clientSecret = null,
                    scope = null,
                    refreshToken = refreshToken,
                    codeVerifier = null,
                    additionalParameters = null,
                )

            val tokenResp = performTokenRequest(tokenRequest)

            val access =
                tokenResp.accessToken
                    ?: throw IllegalStateException("No access token")

            val expiresAtEpochMs =
                tokenResp.accessTokenExpirationDate
                    ?.timeIntervalSince1970
                    ?.let { (it * 1000).toLong() }

            AuthTokens(
                accessToken = access,
                refreshToken = tokenResp.refreshToken ?: refreshToken,
                idToken = tokenResp.idToken,
                expiresAtEpochMs = expiresAtEpochMs,
            )
        }

    actual override suspend fun createLogoutIntent(): PlatformAuthIntent? = null

    actual override suspend fun getEndSessionRequestIntent(idToken: String): PlatformAuthIntent {
        val serviceConfig = discover()
        val redirectUrl =
            NSURL(string = config.redirectUri)
                ?: error("Bad redirectUri: ${config.redirectUri}")

        val request =
            OIDEndSessionRequest(
                configuration = serviceConfig,
                idTokenHint = idToken,
                postLogoutRedirectURL = redirectUrl,
                additionalParameters = null,
            )

        return platformEndSessionIntent(request)
    }
}
