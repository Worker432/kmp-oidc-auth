package io.github.zm.kmpauth

import androidx.compose.ui.window.ComposeUIViewController
import io.github.zm.kmpauth.di.startKoinIos
import io.github.zm.kmpauth.ui.MainApp

fun MainViewController() = ComposeUIViewController {
    startKoinIos()

    MainApp()
}