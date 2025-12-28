import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "com.pr0gramm3r101.utils"
        compileSdk = 36
        minSdk = 24
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.constraintlayout)
            implementation(compose.materialIconsExtended)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.adaptive.android)
            implementation(libs.datastore.preferences)
            implementation(libs.datastore.core)
            implementation(libs.biometric)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.security.crypto)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.darwin)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
            // Multiplatform settings for iOS Keychain support
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.multiplatform.settings.serialization)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.pr0gramm3r101.shared"
    generateResClass = auto
}