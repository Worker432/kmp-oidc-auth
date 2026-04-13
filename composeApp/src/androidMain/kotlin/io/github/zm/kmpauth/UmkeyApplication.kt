package io.github.zm.kmpauth

import android.app.Application
import io.github.zm.kmpauth.di.androidModules
import io.github.zm.kmpauth.di.initKoin
import org.koin.android.ext.koin.androidContext

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            appDeclaration = {
                androidContext(this@TestApplication)
            },
            platformModules = androidModules
        )
    }
}
