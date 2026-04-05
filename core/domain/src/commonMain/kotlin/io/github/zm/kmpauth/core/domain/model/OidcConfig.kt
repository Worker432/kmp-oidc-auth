package io.github.zm.kmpauth.core.domain.model

data class OidcConfig(
    val issuerUrl: String,
    val clientId: String,
    val redirectUri: String,
    val scope: String = "openid profile offline_access",
)
