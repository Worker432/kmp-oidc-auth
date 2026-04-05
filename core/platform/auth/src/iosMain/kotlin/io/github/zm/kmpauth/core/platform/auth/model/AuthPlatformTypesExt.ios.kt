@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import kotlinx.cinterop.ExperimentalForeignApi

fun PlatformLoginIntent.asIosAuthRequest(): OIDAuthorizationRequest = this.request

fun PlatformCallbackPayload.asIosAuthResponse(): OIDAuthorizationResponse? = this.response

fun PlatformCallbackPayload.asIosErrorDescription(): String? = this.errorDescription
