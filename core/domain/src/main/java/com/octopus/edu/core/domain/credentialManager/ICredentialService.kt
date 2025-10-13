package com.octopus.edu.core.domain.credentialManager

import android.content.Context

sealed interface AuthResult {
    data class Success(
        val idToken: String
    ) : AuthResult

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : AuthResult

    data object None : AuthResult
}

sealed interface SignInInitiationResult {
    data class Authenticated(
        val idToken: String
    ) : SignInInitiationResult

    data object NoOp : SignInInitiationResult

    data class Error(
        val message: String
    ) : SignInInitiationResult
}

interface ICredentialService {
    suspend fun initiateGoogleSignIn(context: Context): SignInInitiationResult

    suspend fun clearUserCredentials(onError: (Throwable) -> Unit)
}
