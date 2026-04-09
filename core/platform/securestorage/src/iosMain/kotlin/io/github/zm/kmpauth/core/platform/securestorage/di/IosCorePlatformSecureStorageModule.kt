package io.github.zm.kmpauth.core.platform.securestorage.di

import io.github.zm.kmpauth.core.platform.securestorage.DefaultSecureStorage
import io.github.zm.kmpauth.core.platform.securestorage.SecureStorage
import org.koin.dsl.module

val iosCorePlatformSecureStorageModule = module {
    single<SecureStorage> {
        DefaultSecureStorage()
    }
}