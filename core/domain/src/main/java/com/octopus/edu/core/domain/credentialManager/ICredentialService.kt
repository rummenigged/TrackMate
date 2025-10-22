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
    /**
 * Starts the Google sign-in flow using the provided Android Context and produces a sign-in result.
 *
 * @param context Android Context used to launch the Google sign-in UI.
 * @return A [SignInInitiationResult]: `Authenticated` with an `idToken` on success, `NoOp` if no action was taken, or `Error` with a message on failure.
 */
suspend fun initiateGoogleSignIn(context: Context): SignInInitiationResult

    /**
 * Clears stored user credentials.
 *
 * Invokes `onError` with the encountered `Throwable` if the clearance operation fails; otherwise completes with no result.
 *
 * @param onError Callback invoked when an error occurs while clearing credentials.
 */
suspend fun clearUserCredentials(onError: (Throwable) -> Unit)
}