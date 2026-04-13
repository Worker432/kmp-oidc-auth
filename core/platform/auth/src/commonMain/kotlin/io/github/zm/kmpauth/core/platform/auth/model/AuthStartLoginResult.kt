package io.github.zm.kmpauth.core.platform.auth.model

sealed class AuthStartLoginResult {
    data class Launch(val intent: PlatformAuthIntent) : AuthStartLoginResult()
    data object AlreadyAuthorized : AuthStartLoginResult()
}
