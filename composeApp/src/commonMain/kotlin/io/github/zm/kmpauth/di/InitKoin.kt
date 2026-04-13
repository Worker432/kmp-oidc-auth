package io.github.zm.kmpauth.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
    platformModules: List<Module> = emptyList()
): KoinApplication = startKoin {
    appDeclaration()
    modules(commonModules + platformModules)
}
