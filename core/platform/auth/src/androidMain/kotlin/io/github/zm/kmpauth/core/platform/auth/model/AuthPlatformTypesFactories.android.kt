package io.github.zm.kmpauth.core.platform.auth.model

import android.content.Intent

fun platformLoginIntent(intent: Intent): PlatformAuthIntent = PlatformAuthIntent(intent)

fun platformCallbackPayload(intent: Intent?): PlatformCallbackPayload = PlatformCallbackPayload(intent)
