import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.cocoapods)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
        }
    }

    cocoapods {
        summary = "Auth platform module"
        homepage = "https://ms"
        ios.deploymentTarget = "15.0"
        name = "CorePlatformAuth"
        version = "1.0.0"

        framework {
            baseName = "CorePlatformAuth"
            isStatic = true
        }

        pod("AppAuth") {
            version = "~> 2.0.0"
        }
    }
}

android {
    namespace = "io.github.zm.kmpauth.core.platform.auth"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
}

