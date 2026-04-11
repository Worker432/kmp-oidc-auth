package io.github.zm.kmpauth.core.platform.auth

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.github.zm.kmpauth.core.platform.auth.model.AuthTokens
import io.github.zm.kmpauth.core.platform.auth.model.OidcConfig
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.asAndroidIntent
import io.github.zm.kmpauth.core.platform.auth.model.platformLoginIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume

/**
 * Created by Aldar Baldanov on 07.04.2026
 * baldanovaldar@gmail.com
 */
class AndroidAuthApi(
    context: Context,
    private val config: OidcConfig
) : AuthApi {

    private val service = AuthorizationService(context)

    suspend fun fetchConfig(issuer: Uri): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(issuer) { cfg, ex ->
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

    override suspend fun startLogin(): PlatformAuthIntent {
        val serviceConfig = fetchConfig(
            config.issuerUrl.toUri()
        )

        val request = AuthorizationRequest.Builder(
            serviceConfig,
            config.clientId,
            ResponseTypeValues.CODE,
            config.redirectUri.toUri()
        ).setScope(config.scope).build()

        return platformLoginIntent(service.getAuthorizationRequestIntent(request))
    }

    override suspend fun completeLogin(payload: PlatformCallbackPayload): AuthTokens {
        val intent = payload.asAndroidIntent()!!

        val response = AuthorizationResponse.fromIntent(intent)
            ?: error("No response")

        val tokenRequest = response.createTokenExchangeRequest()

        val (tokenResponse, _) = suspendCancellableCoroutine { cont ->
            service.performTokenRequest(tokenRequest) { resp, ex ->
                cont.resume(resp to ex)
            }
        }

        return AuthTokens(
            accessToken = tokenResponse!!.accessToken!!,
            refreshToken = tokenResponse.refreshToken,
            idToken = tokenResponse.idToken,
            expiresAtEpochMs = tokenResponse.accessTokenExpirationTime
        )
    }

    override suspend fun refresh(refreshToken: String): AuthTokens {
        TODO()
    }
}