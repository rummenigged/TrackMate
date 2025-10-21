plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.core.network"
    compileSdk = rootProject.extra["compileSdkVersion"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"].toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "boolean",
            "USE_FIREBASE_EMULATOR",
            rootProject.extra["USE_FIREBASE_EMULATOR"].toString(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"192.168.0.3\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(project(":core:common"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.converter)
    ksp(libs.moshi.kotlinCodeGen)

    implementation(platform(libs.firebase.bom))
    api(libs.firebase.firestore)
    api(libs.firebase.auth)
}
