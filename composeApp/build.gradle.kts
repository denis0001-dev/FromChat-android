
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.22"
}

    kotlin {
    androidTarget()

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
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.adaptive.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.core)
            implementation(libs.slf4j.android)
            implementation(libs.material)
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
            
            implementation(project(":utils"))
        }

        iosMain.dependencies {
            implementation(libs.jetbrains.kotlinx.io.bytestring)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "ru.fromchat"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.fromchat"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val keystoreProperties = Properties().apply {
                load(FileInputStream(rootProject.file("keys/keystore.properties")))
            }
            storeFile = rootProject.file("keys/release.jks")
            keyAlias = "release"
            storePassword = keystoreProperties["storePassword"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
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