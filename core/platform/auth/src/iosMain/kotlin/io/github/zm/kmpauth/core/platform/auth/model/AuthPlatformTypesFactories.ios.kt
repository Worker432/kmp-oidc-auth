@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import cocoapods.AppAuth.OIDEndSessionRequest
import kotlinx.cinterop.ExperimentalForeignApi

fun platformLoginIntent(request: OIDAuthorizationRequest): PlatformAuthIntent =
    PlatformAuthIntent(
        authorizationRequest = request,
        endSessionRequest = null,
    )

fun platformEndSessionIntent(request: OIDEndSessionRequest): PlatformAuthIntent =
    PlatformAuthIntent(
        authorizationRequest = null,
        endSessionRequest = request,
    )

fun platformCallbackPayload(
    response: OIDAuthorizationResponse?,
    errorDescription: String? = null,
): PlatformCallbackPayload =
    PlatformCallbackPayload(response, errorDescription)
