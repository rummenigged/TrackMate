plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.core.common"
    compileSdk = rootProject.extra["compileSdkVersion"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"].toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = rootProject.ext["sourceCompatibility"] as JavaVersion
        targetCompatibility = rootProject.ext["targetCompatibility"] as JavaVersion
    }

    kotlin {
        jvmToolchain(rootProject.ext["kotlinOptionsJVMTarget"].toString().toInt())
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.coreKtx)
    implementation(libs.credentials)
    implementation(libs.identity.googleId)
    implementation(libs.credentials.playService.auth)

    api(libs.timber)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
