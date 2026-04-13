package io.github.zm.kmpauth.di

import io.github.zm.kmpauth.core.platform.auth.di.iosCorePlatformAuthModule
import io.github.zm.kmpauth.core.platform.securestorage.di.iosCorePlatformSecureStorageModule

val iosModules = listOf(
    iosCorePlatformAuthModule,
    iosCorePlatformSecureStorageModule,
)
