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
    /**
     * Creates an AuthAdapter implementation that uses the provided FirebaseAuth instance.
     *
     * @param firebaseAuth The FirebaseAuth instance used by the adapter to perform authentication operations.
     * @return An AuthAdapter configured to delegate authentication to the given FirebaseAuth.
     */
    @Provides
    fun provideFirebaseAuthAdapter(firebaseAuth: FirebaseAuth): AuthAdapter = FirebaseAuthAdapter(firebaseAuth)

    /**
         * Provides an AuthRepository backed by the given AuthAdapter and DispatcherProvider.
         *
         * @param dispatcherProvider Supplies coroutine dispatchers used by repository operations.
         * @param authAdapter Adapter that performs authentication operations (e.g., sign-in, sign-out).
         * @return An AuthRepository instance wired with the provided adapter and dispatcher provider.
         */
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