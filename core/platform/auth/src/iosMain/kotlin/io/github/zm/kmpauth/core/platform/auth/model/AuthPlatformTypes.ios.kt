@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import cocoapods.AppAuth.OIDEndSessionRequest
import kotlinx.cinterop.ExperimentalForeignApi

actual class PlatformAuthIntent internal constructor(
    internal val authorizationRequest: OIDAuthorizationRequest?,
    internal val endSessionRequest: OIDEndSessionRequest?,
)

actual class PlatformCallbackPayload internal constructor(
    internal val response: OIDAuthorizationResponse?,
    internal val errorDescription: String?,
)
