package io.github.zm.kmpauth.core.platform.auth.model

sealed class AuthFlowResult {
    data class Launch(
        val intent: PlatformAuthIntent,
    ) : AuthFlowResult()

    data object AlreadyAuthorized : AuthFlowResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : AuthFlowResult()
}
