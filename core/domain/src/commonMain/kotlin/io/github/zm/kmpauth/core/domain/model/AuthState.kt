package io.github.zm.kmpauth.core.domain.model

sealed class AuthState {
    data object Loading : AuthState()

    data object Unauthorized : AuthState()

    data object Authorized : AuthState()
}
