package io.github.zm.kmpauth.core.platform.auth.di

import org.koin.dsl.module
import io.github.zm.kmpauth.core.platform.auth.manager.AuthManager
import io.github.zm.kmpauth.core.platform.auth.manager.AuthManagerImpl
import io.github.zm.kmpauth.core.platform.auth.model.OidcConfig
import io.github.zm.kmpauth.core.platform.auth.tokenStore.TokenStore

val platformAuthModule = module {
    single {
        OidcConfig(
            issuerUrl = "",
            clientId = "",
            redirectUri = "",
            scope = "",
        )
    }
    single {
        TokenStore(get())
    }
    single<AuthManager> { AuthManagerImpl(get(), get(),get()) }
}
