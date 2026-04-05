@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import kotlinx.cinterop.ExperimentalForeignApi

fun platformLoginIntent(request: OIDAuthorizationRequest): PlatformLoginIntent =
    PlatformLoginIntent(request)

fun platformCallbackPayload(
    response: OIDAuthorizationResponse?,
    errorDescription: String? = null,
): PlatformCallbackPayload =
    PlatformCallbackPayload(response, errorDescription)
