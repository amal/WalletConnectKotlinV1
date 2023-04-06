@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.app.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.kotlin.ksp)
}

android {
    val ns = "io.walletconnect.example"
    namespace = ns

    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    buildToolsVersion = libs.versions.androidBuildTools.get()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions.jvmTarget = "1.8"

    defaultConfig {
        applicationId = ns
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidCompileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    lint {
        textReport = true
        textOutput = File("stdout")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.lib)

    implementation(libs.java.websocket)

    implementation(libs.khex)

    implementation(kotlin("stdlib"))

    implementation(libs.androidx.activity)

    implementation(libs.moshi)
    implementation(libs.okhttp)

    implementation(libs.kotlinx.coroutines.android)
}
