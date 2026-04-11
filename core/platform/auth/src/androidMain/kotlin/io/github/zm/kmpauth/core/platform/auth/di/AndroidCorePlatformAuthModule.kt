package io.github.zm.kmpauth.core.platform.auth.di

import io.github.zm.kmpauth.core.platform.auth.driver.AuthDriver
import io.github.zm.kmpauth.core.platform.auth.driver.DefaultAuthDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidCorePlatformAuthModule =
    module {
        single<AuthDriver> {
            DefaultAuthDriver(
                get(),
                get(),
            )
        }
    }
