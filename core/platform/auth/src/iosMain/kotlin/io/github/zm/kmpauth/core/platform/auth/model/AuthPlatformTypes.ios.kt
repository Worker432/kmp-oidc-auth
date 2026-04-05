@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import kotlinx.cinterop.ExperimentalForeignApi

actual class PlatformLoginIntent internal constructor(
    internal val request: OIDAuthorizationRequest,
)

actual class PlatformCallbackPayload internal constructor(
    internal val response: OIDAuthorizationResponse?,
    internal val errorDescription: String?,
)