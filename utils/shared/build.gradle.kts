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
        }

        androidMain.dependencies {
            implementation(libs.androidx.adaptive.android)
            implementation(libs.datastore.preferences)
            implementation(libs.datastore.core)
            implementation(libs.biometric)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
        }

        iosMain.dependencies {
            // nothing
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.pr0gramm3r101.shared"
    generateResClass = auto
}