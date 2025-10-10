package com.octopus.edu.core.network.utils.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.network.BuildConfig
import com.octopus.edu.core.network.BuildConfig.USE_FIREBASE_EMULATOR

object FirebaseProvider {
    fun getFirestore(): FirebaseFirestore =
        Firebase.firestore.apply {
            if (USE_FIREBASE_EMULATOR) {
                try {
                    useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 8080)
                } catch (e: IllegalStateException) {
                    Logger.w(
                        message = "Firestore emulator already configured or instance used",
                        throwable = e,
                    )
                }
            }
        }
}
