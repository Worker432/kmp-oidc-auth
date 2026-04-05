package io.github.zm.kmpauth.core.platform.auth.model

import android.content.Intent

fun PlatformLoginIntent.asAndroidIntent(): Intent = this.intent

fun PlatformCallbackPayload.asAndroidIntent(): Intent? = this.intent
