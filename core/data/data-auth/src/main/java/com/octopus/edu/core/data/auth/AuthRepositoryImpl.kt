package com.octopus.edu.core.data.auth

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapter
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.AuthRepository
import com.octopus.edu.core.domain.utils.safeCall
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl
    @Inject
    constructor(
        private val firebaseAuthAdapter: FirebaseAuthAdapter,
        private val dispatcherProvider: DispatcherProvider
    ) : AuthRepository {
        override val isUserLoggedIn: Flow<Boolean>
            get() = firebaseAuthAdapter.isUserLoggedIn()

        override suspend fun signIn(token: String): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                firebaseAuthAdapter.signInWithCredentials(token)
            }

        override suspend fun signOut(): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                firebaseAuthAdapter.signOut()
            }
    }
