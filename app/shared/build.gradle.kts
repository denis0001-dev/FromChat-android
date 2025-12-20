
import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    androidLibrary {
        namespace = "ru.fromchat.shared"
        minSdk = 24
        compileSdk = 36
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.constraintlayout)
            implementation(libs.navigation.compose)
            implementation(compose.materialIconsExtended)
            implementation(libs.haze)
            implementation(libs.haze.materials)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.io.core)

            // Ktor - force version 2.3.12 to avoid conflicts with Coil 3's Ktor 3
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.logging)

            // Datetime
            implementation(libs.kotlinx.datetime)

            // Coil for image loading (multiplatform)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(project(":utils:shared"))
        }

        iosMain.dependencies {
            implementation(libs.jetbrains.kotlinx.io.bytestring)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ru.fromchat"
    generateResClass = auto
}

tasks.register("generateResourceAccessors") {
    dependsOn(
        *(
            tasks.filter {
                it.name.startsWith("generateResourceAccessors") &&
                !it.name.matches("^(:${project.name})?generateResourceAccessors$".toRegex())
            }.toTypedArray()
        )
    )
}