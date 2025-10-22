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

        /**
             * Signs in a user using the provided authentication token.
             *
             * @param token The authentication token to use for sign-in.
             * @return `Unit` if sign-in succeeds, error information otherwise wrapped in a `ResultOperation`.
             */
            override suspend fun signIn(token: String): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                authAdapter.signInWithCredentials(token)
            }

        /**
             * Signs out the currently authenticated user.
             *
             * @return `ResultOperation<Unit>` representing the success or failure of the sign-out operation.
             */
            override suspend fun signOut(): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                authAdapter.signOut()
            }
    }