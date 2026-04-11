package io.github.zm.kmpauth.core.platform.auth.model

data class OidcConfig(
    val issuerUrl: String,
    val clientId: String,
    val redirectUri: String,
    val scope: String = "openid profile offline_access sip",
)

fun OidcConfig.isValid(): Boolean =
    issuerUrl.isNotBlank() && clientId.isNotBlank() && redirectUri.isNotBlank()