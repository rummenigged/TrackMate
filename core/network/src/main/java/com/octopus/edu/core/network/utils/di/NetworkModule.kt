package com.octopus.edu.core.network.utils.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * Provides a FirebaseFirestore instance for injection.
     *
     * @return A FirebaseFirestore instance used for Firestore operations.
     */
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseProvider.getFirestore()

    /**
     * Supply the Firebase Authentication client used to authenticate users.
     *
     * @return The FirebaseAuth instance used to perform user authentication.
     */
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseProvider.getFirebaseAuth()
}