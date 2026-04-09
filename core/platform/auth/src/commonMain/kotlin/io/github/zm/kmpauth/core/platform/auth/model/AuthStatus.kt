package io.github.zm.kmpauth.core.platform.auth.model

sealed class AuthStatus {
    data object Loading : AuthStatus()
    data object Unauthorized : AuthStatus()
    data object Authorized : AuthStatus()
}

