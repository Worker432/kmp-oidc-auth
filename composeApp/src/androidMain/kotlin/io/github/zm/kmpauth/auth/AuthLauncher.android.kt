package io.github.zm.kmpauth.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import io.github.zm.kmpauth.core.platform.auth.model.PlatformAuthIntent
import io.github.zm.kmpauth.core.platform.auth.model.PlatformCallbackPayload
import io.github.zm.kmpauth.core.platform.auth.model.asAndroidIntent
import io.github.zm.kmpauth.core.platform.auth.model.platformCallbackPayload

private class AndroidAuthLauncher(
    private val doLaunch: (PlatformAuthIntent) -> Unit,
) : AuthLauncher {
    override fun launch(intent: PlatformAuthIntent) = doLaunch(intent)
}

@Composable
actual fun rememberAuthLauncher(onResult: (PlatformCallbackPayload) -> Unit): AuthLauncher {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            onResult(platformCallbackPayload(result.data))
        }

    return AndroidAuthLauncher { platformIntent ->
        launcher.launch(platformIntent.asAndroidIntent())
    }
}
