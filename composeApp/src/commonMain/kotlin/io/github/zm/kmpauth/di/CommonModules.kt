package io.github.zm.kmpauth.di

import io.github.zm.kmpauth.core.platform.auth.di.platformAuthModule
import org.koin.dsl.module

val commonModules = module {
    includes(
        platformAuthModule,
    )
}
