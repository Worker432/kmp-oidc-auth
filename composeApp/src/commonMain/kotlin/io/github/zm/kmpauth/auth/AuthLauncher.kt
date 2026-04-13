package io.github.zm.kmpauth.auth

import androidx.compose.runtime.Composable
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload

interface AuthLauncher {
    fun launch(intent: PlatformAuthIntent)
}

@Composable
expect fun rememberAuthLauncher(onResult: (PlatformCallbackPayload) -> Unit): AuthLauncher

