package io.github.zm.kmpauth.core.platform.auth.di

import io.github.zm.kmpauth.core.platform.auth.driver.AuthDriver
import io.github.zm.kmpauth.core.platform.auth.driver.DefaultAuthDriver
import org.koin.dsl.module

val iosCorePlatformAuthModule =
    module {
        single<AuthDriver> {
            DefaultAuthDriver(
                get(),
            )
        }
    }
