
import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

val fixComposeResourcesStructure = tasks.register<Copy>("fixComposeResourcesStructure") {
    val sharedProject = rootProject.project(":app:shared")

    from(
        sharedProject
            .layout
            .buildDirectory
            .dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources")
    )

    into(
        layout
            .buildDirectory
            .dir("intermediates/fixed_compose_res/composeResources/ru.fromchat")
    )

    dependsOn(sharedProject.tasks.matching { it.name.contains("prepareComposeResources", ignoreCase = true) })
    dependsOn(sharedProject.tasks.matching { it.name.contains("copyNonXmlValueResources", ignoreCase = true) })
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
                load(FileInputStream(file("keys/keystore.properties")))
            }

            storeFile = file("keys/release.jks")
            keyAlias = "key0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].apply {
        assets.srcDirs(
            layout.buildDirectory.dir("intermediates/fixed_compose_res")
        )
    }
}

tasks.withType<MergeSourceSetFolders>().configureEach {
    dependsOn(fixComposeResourcesStructure)
}

tasks.matching { it.name.contains("lintVital", ignoreCase = true) }.configureEach {
    dependsOn(fixComposeResourcesStructure)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.adaptive.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.slf4j.android)
    implementation(libs.material)

    implementation(project(":app:shared"))
    implementation(project(":utils:shared"))
}