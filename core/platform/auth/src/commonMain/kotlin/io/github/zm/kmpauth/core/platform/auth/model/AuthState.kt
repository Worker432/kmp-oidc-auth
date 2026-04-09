package io.github.zm.kmpauth.core.platform.auth.model

sealed class AuthState {
    data object Loading : AuthState()

    data object Unauthorized : AuthState()

    data object Authorized : AuthState()

    data object ConfigInvalid: AuthState()

    data object NeedRefresh: AuthState()
}
