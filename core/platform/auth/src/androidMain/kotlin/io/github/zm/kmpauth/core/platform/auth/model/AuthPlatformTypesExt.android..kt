package io.github.zm.kmpauth.core.platform.auth.model

import android.content.Intent

fun PlatformAuthIntent.asAndroidIntent(): Intent = this.intent

fun PlatformCallbackPayload.asAndroidIntent(): Intent? = this.intent
