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
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseProvider.getFirestore()

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseProvider.getFirebaseAuth()
}
