
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.firebase.appdistribution) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    dependencies {
        classpath(libs.ktlint.gradlePlugin)
    }

    extra.apply {
        set("applicationId", "com.octopus.edu.trackmate")
        set("compileSdkVersion", 36)
        set("targetSdkVersion", 36)
        set("minSdkVersion", 28)
        set("targetCompatibility", JavaVersion.VERSION_17)
        set("sourceCompatibility", JavaVersion.VERSION_17)
        set("kotlinOptionsJVMTarget", "17")
    }
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        verbose = true
        android = true
    }
}
