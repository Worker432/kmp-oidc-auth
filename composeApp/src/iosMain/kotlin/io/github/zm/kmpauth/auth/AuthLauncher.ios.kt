@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import cocoapods.AppAuth.OIDAuthorizationService
import cocoapods.AppAuth.OIDExternalUserAgentIOS
import cocoapods.AppAuth.presentAuthorizationRequest
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.asIosAuthRequest
import io.github.zm.kmpauth.core.platform.auth.model.asIosEndSessionRequest
import io.github.zm.kmpauth.core.platform.auth.model.platformCallbackPayload
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSArray
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

private fun keyWindowSafe(): UIWindow? {
    val app = UIApplication.sharedApplication

    app.keyWindow?.let { return it }

    val windowsAny = app.windows
    val windows = windowsAny as? NSArray ?: return null
    if (windows.count.toInt() == 0) return null

    return windows.objectAtIndex(0u) as? UIWindow
}

private fun topMostViewController(): UIViewController? {
    var vc = keyWindowSafe()?.rootViewController ?: return null

    while (true) {
        vc.presentedViewController?.let {
            vc = it
            continue
        }

        when (vc) {
            is UINavigationController -> {
                vc = (vc.visibleViewController ?: vc.topViewController) ?: vc
                continue
            }
            is UITabBarController -> {
                vc.selectedViewController?.let {
                    vc = it
                    continue
                }
            }
        }

        return vc
    }
}

private class IosAuthLauncher(
    private val onResult: (PlatformCallbackPayload) -> Unit,
) : AuthLauncher {
    private var activeSession: Any? = null

    override fun launch(intent: PlatformAuthIntent) {
        val vc = topMostViewController()
        if (vc == null) {
            onResult(
                platformCallbackPayload(
                    response = null,
                    errorDescription = "No UIViewController to present auth UI",
                ),
            )
            return
        }

        val authRequest = intent.asIosAuthRequest()
        if (authRequest != null) {
            activeSession =
                OIDAuthorizationService.presentAuthorizationRequest(
                    request = authRequest,
                    presentingViewController = vc,
                ) { response, error ->
                    activeSession = null

                    // AppAuth iOS: general error -3 = user cancelled
                    val isUserCancelled =
                        error != null && error.domain == "org.openid.appauth.general" && error.code.toInt() == -3

                    if (isUserCancelled) {
                        onResult(platformCallbackPayload(response = null, errorDescription = ""))
                        return@presentAuthorizationRequest
                    }

                    onResult(platformCallbackPayload(response, error?.localizedDescription))
                }
            return
        }

        val endSessionRequest = intent.asIosEndSessionRequest()
        if (endSessionRequest == null) {
            onResult(
                platformCallbackPayload(
                    response = null,
                    errorDescription = "No iOS auth request to launch",
                ),
            )
            return
        }

        val externalUserAgent = OIDExternalUserAgentIOS(presentingViewController = vc)
        if (externalUserAgent == null) {
            onResult(
                platformCallbackPayload(
                    response = null,
                    errorDescription = "Failed to create iOS external user agent",
                ),
            )
            return
        }

        activeSession =
            OIDAuthorizationService.presentEndSessionRequest(
                request = endSessionRequest,
                externalUserAgent = externalUserAgent,
            ) { _, error ->
                activeSession = null

                val isUserCancelled =
                    error != null && error.domain == "org.openid.appauth.general" && error.code.toInt() == -3

                if (isUserCancelled) {
                    onResult(platformCallbackPayload(response = null, errorDescription = ""))
                    return@presentEndSessionRequest
                }

                onResult(
                    platformCallbackPayload(
                        response = null,
                        errorDescription = error?.localizedDescription,
                    ),
                )
            }
    }
}

@Composable
actual fun rememberAuthLauncher(onResult: (PlatformCallbackPayload) -> Unit): AuthLauncher {
    val latestOnResult = rememberUpdatedState(onResult)

    return remember {
        IosAuthLauncher { payload -> latestOnResult.value(payload) }
    }
}
