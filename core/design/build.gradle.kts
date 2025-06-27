plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.octopus.edu.core.design"
    compileSdk = rootProject.extra["compileSdkVersion"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"].toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = rootProject.ext["sourceCompatibility"] as JavaVersion
        targetCompatibility = rootProject.ext["targetCompatibility"] as JavaVersion
    }

    kotlin {
        jvmToolchain(rootProject.ext["kotlinOptionsJVMTarget"].toString().toInt())
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    api(libs.compose.material3)
}
