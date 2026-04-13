package io.github.zm.kmpauth.di

import org.koin.core.Koin

private object KoinHolder {
    var koin: Koin? = null
}

fun startKoinIos(): Koin {
    KoinHolder.koin?.let { return it }
    val koin = initKoin(platformModules =
        iosModules
    ).koin
    KoinHolder.koin = koin

    return koin
}