package com.octopus.edu.core.data.auth

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.auth.authAdapter.AuthAdapter
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.AuthRepository
import com.octopus.edu.core.domain.utils.safeCall
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl
    @Inject
    constructor(
        private val authAdapter: AuthAdapter,
        private val dispatcherProvider: DispatcherProvider
    ) : AuthRepository {
        override val isUserLoggedIn: Flow<Boolean>
            get() = authAdapter.isUserLoggedIn()

        override suspend fun signIn(token: String): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                authAdapter.signInWithCredentials(token)
            }

        override suspend fun signOut(): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                authAdapter.signOut()
            }
    }
