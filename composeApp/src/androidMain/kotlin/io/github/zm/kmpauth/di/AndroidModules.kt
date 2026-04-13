package io.github.zm.kmpauth.di

import io.github.zm.kmpauth.core.platform.auth.di.androidCorePlatformAuthModule
import io.github.zm.kmpauth.core.platform.securestorage.di.androidCorePlatformSecureStorageModule

val androidModules = listOf(
    androidCorePlatformAuthModule,
    androidCorePlatformSecureStorageModule,
)
