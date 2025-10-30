package com.octopus.edu.core.data.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.auth.AuthRepositoryImpl
import com.octopus.edu.core.data.auth.authAdapter.AuthAdapter
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapter
import com.octopus.edu.core.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AuthDataModule {
    @Provides
    fun provideFirebaseAuthAdapter(firebaseAuth: FirebaseAuth): AuthAdapter = FirebaseAuthAdapter(firebaseAuth)

    @Provides
    fun provideAuthRepository(
        dispatcherProvider: DispatcherProvider,
        authAdapter: AuthAdapter
    ): AuthRepository =
        AuthRepositoryImpl(
            authAdapter,
            dispatcherProvider,
        )
}
