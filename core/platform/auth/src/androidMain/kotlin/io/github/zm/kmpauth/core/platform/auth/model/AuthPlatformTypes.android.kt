package io.github.zm.kmpauth.core.platform.auth.model

import android.content.Intent

actual class PlatformAuthIntent internal constructor(
    internal val intent: Intent,
)

actual class PlatformCallbackPayload internal constructor(
    internal val intent: Intent?,
)