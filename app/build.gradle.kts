import java.util.Properties

plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.firebase.appdistribution)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = rootProject.extra["applicationId"].toString()
    compileSdk = rootProject.extra["compileSdkVersion"].toString().toInt()

    defaultConfig {
        applicationId = rootProject.extra["applicationId"].toString()
        minSdk = rootProject.extra["minSdkVersion"].toString().toInt()
        targetSdk = rootProject.extra["targetSdkVersion"].toString().toInt()
        versionCode = 2
        versionName = "0.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {

        val keyStorePropsFile = rootProject.file("keystore.properties")
        if (keyStorePropsFile.exists()) {
            val props =
                Properties().apply {
                    load(keyStorePropsFile.inputStream())
                }

            create("release") {
                storeFile = file(props["release.keyStore"] as String)
                storePassword = props["release.storePassword"] as String
                keyAlias = props["release.keyAlias"] as String
                keyPassword = props["release.keyPassword"] as String
            }
        } else {
            // fallback for local dev
            create("release") {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isProfileable = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            manifestPlaceholders["usesClearTextTraffic"] = false
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            isDebuggable = true

            manifestPlaceholders["usesClearTextTraffic"] = true
        }
    }

    compileOptions {
        sourceCompatibility = rootProject.ext["sourceCompatibility"] as JavaVersion
        targetCompatibility = rootProject.ext["targetCompatibility"] as JavaVersion
    }

    kotlin {
        jvmToolchain(rootProject.ext["kotlinOptionsJVMTarget"].toString().toInt())
    }

    lint {
        warningsAsErrors = true
        checkDependencies = true
        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.addAll(
                setOf(
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md",
                ),
            )
        }
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask> {
    workerMaxHeapSize.set("512m")
}

dependencies {

    implementation(project(":core:design"))
    implementation(project(":core:data:data-entry"))
    implementation(project(":core:data:database"))
    implementation(project(":core:ui-common"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":feature:signIn"))
    implementation(project(":feature:home"))
    implementation(project(":feature:history"))
    implementation(project(":feature:analytics"))

    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.navigation.fragmentKtx)
    implementation(libs.navigation.ui)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.suite.android)
    implementation(libs.firebase.crashlytics)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.test.performance.runtime.tracing)
    implementation(libs.test.performance.tracingKtx)

    implementation(libs.work.runtime)

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.auto.value.annotations)
    // TODO: Migrate to KSP when the com.google.auto.value:auto-value becomes compatible with it
    kapt(libs.auto.value)

    implementation(libs.performance.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
}
