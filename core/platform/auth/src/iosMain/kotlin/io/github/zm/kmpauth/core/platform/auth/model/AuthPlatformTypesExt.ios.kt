@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.auth.model

import cocoapods.AppAuth.OIDAuthorizationRequest
import cocoapods.AppAuth.OIDAuthorizationResponse
import cocoapods.AppAuth.OIDEndSessionRequest
import kotlinx.cinterop.ExperimentalForeignApi

fun PlatformAuthIntent.asIosAuthRequest(): OIDAuthorizationRequest? = this.authorizationRequest

fun PlatformAuthIntent.asIosEndSessionRequest(): OIDEndSessionRequest? = this.endSessionRequest

fun PlatformCallbackPayload.asIosAuthResponse(): OIDAuthorizationResponse? = this.response

fun PlatformCallbackPayload.asIosErrorDescription(): String? = this.errorDescription
