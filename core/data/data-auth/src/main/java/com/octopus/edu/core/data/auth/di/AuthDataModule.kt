package com.octopus.edu.core.data.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.auth.AuthRepositoryImpl
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapter
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapterImpl
import com.octopus.edu.core.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AuthDataModule {
    @Provides
    fun provideFirebaseAuthAdapter(firebaseAuth: FirebaseAuth): FirebaseAuthAdapter = FirebaseAuthAdapterImpl(firebaseAuth)

    @Provides
    fun provideAuthRepository(
        dispatcherProvider: DispatcherProvider,
        firebaseAuthAdapter: FirebaseAuthAdapter
    ): AuthRepository =
        AuthRepositoryImpl(
            firebaseAuthAdapter,
            dispatcherProvider,
        )

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
