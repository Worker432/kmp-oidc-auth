package io.github.zm.kmpauth.core.platform.auth.model

import android.content.Intent

fun platformLoginIntent(intent: Intent): PlatformLoginIntent = PlatformLoginIntent(intent)

fun platformCallbackPayload(intent: Intent?): PlatformCallbackPayload = PlatformCallbackPayload(intent)
