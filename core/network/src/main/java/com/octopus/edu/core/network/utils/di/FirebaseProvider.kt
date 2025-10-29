package com.octopus.edu.core.network.utils.di

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.network.BuildConfig
import com.octopus.edu.core.network.BuildConfig.USE_FIREBASE_EMULATOR

object FirebaseProvider {
    private val host =
        if (isEmulator()) {
            BuildConfig.FIREBASE_EMULATOR_HOST_VIRTUAL_DEVICE
        } else {
            BuildConfig.FIREBASE_EMULATOR_HOST_PHYSICAL_DEVICE
        }

    fun getFirestore(): FirebaseFirestore =
        Firebase.firestore.apply {
            if (USE_FIREBASE_EMULATOR) {
                try {
                    useEmulator(host, 8080)
                } catch (e: IllegalStateException) {
                    Logger.w(
                        message = "Firestore emulator already configured or instance used",
                        throwable = e,
                    )
                }
            }
        }

    fun getFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance().apply {
            if (USE_FIREBASE_EMULATOR) {
                try {
                    useEmulator(host, 9099)
                } catch (e: IllegalStateException) {
                    Logger.w(
                        message = "Firebase Auth emulator already configured or instance used",
                        throwable = e,
                    )
                }
            }
        }

    private fun isEmulator(): Boolean =
        (
            Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.lowercase().contains("vbox") ||
                Build.FINGERPRINT.lowercase().contains("test-keys") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT
        )
}
