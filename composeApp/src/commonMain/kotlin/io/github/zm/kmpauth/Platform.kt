package io.github.zm.kmpauth

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform