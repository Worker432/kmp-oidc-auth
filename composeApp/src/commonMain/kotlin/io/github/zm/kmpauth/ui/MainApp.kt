package io.github.zm.kmpauth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zm.kmpauth.auth.rememberAuthLauncher
import io.github.zm.kmpauth.core.platform.auth.manager.AuthManager
import io.github.zm.kmpauth.core.platform.auth.model.AuthFlowResult
import kotlinx.coroutines.launch

private enum class PendingFlow {
    NONE,
    LOGIN,
    LOGOUT,
}

@Composable
fun MainApp() {
    val manager = remember { org.koin.mp.KoinPlatform.getKoin().get<AuthManager>() }
    val scope = rememberCoroutineScope()
    val state by manager.authState.collectAsState()
    var inProgress by remember { mutableStateOf(false) }
    var lastEvent by remember { mutableStateOf("Idle") }
    var pendingFlow by remember { mutableStateOf(PendingFlow.NONE) }

    val launcher = rememberAuthLauncher { payload ->
        scope.launch {
            when (pendingFlow) {
                PendingFlow.LOGIN -> {
                    val result = manager.completeLogin(payload)
                    inProgress = false
                    pendingFlow = PendingFlow.NONE
                    lastEvent =
                        if (result.isSuccess) {
                            "Login completed"
                        } else {
                            "Login failed: ${result.exceptionOrNull()?.message ?: "unknown"}"
                        }
                }

                PendingFlow.LOGOUT -> {
                    val result = manager.completeLogout(payload)
                    inProgress = false
                    pendingFlow = PendingFlow.NONE
                    lastEvent =
                        if (result.isSuccess) {
                            "Logout completed"
                        } else {
                            "Logout failed: ${result.exceptionOrNull()?.message ?: "unknown"}"
                        }
                }

                PendingFlow.NONE -> {
                    inProgress = false
                    lastEvent = "Callback received without pending flow"
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("State: $state")
        Text("In progress: $inProgress")
        Text("Last event: $lastEvent")

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            enabled = !inProgress,
            onClick = {
                scope.launch {
                    inProgress = true
                    when (val result = manager.startLogin()) {
                        is AuthFlowResult.Launch -> {
                            pendingFlow = PendingFlow.LOGIN
                            lastEvent = "Login launched"
                            launcher.launch(result.intent)
                        }
                        is AuthFlowResult.AlreadyAuthorized -> {
                            inProgress = false
                            lastEvent = "Already authorized"
                        }
                        is AuthFlowResult.Error -> {
                            inProgress = false
                            lastEvent = "Start login failed: ${result.message}"
                        }
                    }
                }
            },
        ) {
            Text("Start login")
        }

        Button(
            enabled = !inProgress,
            onClick = {
                scope.launch {
                    val tokenResult = manager.getAccessToken()
                    lastEvent =
                        if (tokenResult.isSuccess) {
                            "Access token acquired"
                        } else {
                            "Get token failed: ${tokenResult.exceptionOrNull()?.message ?: "unknown"}"
                        }
                }
            },
        ) {
            Text("Get token")
        }

        Button(
            enabled = !inProgress,
            onClick = {
                scope.launch {
                    val refreshResult = manager.refreshTokens()
                    lastEvent =
                        if (refreshResult.isSuccess) {
                            "Refresh successful"
                        } else {
                            "Refresh failed: ${refreshResult.exceptionOrNull()?.message ?: "unknown"}"
                        }
                }
            },
        ) {
            Text("Refresh")
        }

        Button(
            enabled = !inProgress,
            onClick = {
                scope.launch {
                    inProgress = true
                    runCatching {
                        manager.getEndSessionRequestIntent()
                    }.onSuccess { intent ->
                        pendingFlow = PendingFlow.LOGOUT
                        lastEvent = "Logout launched"
                        launcher.launch(intent)
                    }.onFailure { error ->
                        inProgress = false
                        pendingFlow = PendingFlow.NONE
                        lastEvent = "Logout start failed: ${error.message ?: "unknown"}"
                    }
                }
            },
        ) {
            Text("Logout")
        }
    }
}
