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

    /**
         * Provides a configured Firestore instance, optionally routed to the local emulator.
         *
         * Returns a FirebaseFirestore instance; when `USE_FIREBASE_EMULATOR` is true the instance
         * will be configured to use the emulator at the resolved host and port 8080 (if configuration is possible).
         *
         * @return A configured `FirebaseFirestore` instance.
         */
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

    /**
         * Provide a FirebaseAuth instance configured to use the Firebase Auth emulator when enabled.
         *
         * If the `USE_FIREBASE_EMULATOR` flag is true, the instance is configured to use the emulator at the selected host and port 9099.
         * If emulator configuration has already been applied, the resulting IllegalStateException is caught and a warning is logged.
         *
         * @return A configured `FirebaseAuth` instance.
         */
        fun getFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance().apply {
            if (USE_FIREBASE_EMULATOR) {
                try {
                    useEmulator(host, 9099)
                } catch (e: IllegalStateException) {
                    Logger.w(
                        message = "Firestore emulator already configured or instance used",
                        throwable = e,
                    )
                }
            }
        }

    /**
         * Determines whether the app is running on an Android emulator.
         *
         * Checks common Android Build properties for known emulator indicators.
         *
         * @return `true` if the runtime appears to be an Android emulator, `false` otherwise.
         */
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