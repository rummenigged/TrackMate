package com.octopus.edu.core.common.credentialService

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.octopus.edu.core.domain.credentialManager.ICredentialService
import com.octopus.edu.core.domain.credentialManager.SignInInitiationResult
import javax.inject.Inject

class AndroidCredentialManagerService
    @Inject
    constructor(
        private val credentialRequest: GetCredentialRequest,
        private val credentialManager: CredentialManager,
    ) : ICredentialService {
        override suspend fun initiateGoogleSignIn(context: Context): SignInInitiationResult =
            try {
                val credential =
                    credentialManager
                        .getCredential(
                            context,
                            credentialRequest,
                        ).credential

                when (credential) {
                    is GoogleIdTokenCredential -> {
                        SignInInitiationResult.Authenticated(credential.idToken)
                    }

                    is CustomCredential -> {
                        if (credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val idTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            SignInInitiationResult.Authenticated(idTokenCredential.idToken)
                        } else {
                            SignInInitiationResult.Error("Unexpected credential type from CredentialManager.")
                        }
                    }

                    else -> SignInInitiationResult.Error("Unexpected credential type from CredentialManager.")
                }
            } catch (_: GetCredentialCancellationException) {
                SignInInitiationResult.NoOp
            } catch (_: NoCredentialException) {
                SignInInitiationResult.Error("No Google credentials found via CredentialManager.")
            } catch (e: GetCredentialException) {
                SignInInitiationResult.Error("CredentialManager error: ${e.message}")
            } catch (e: Exception) {
                SignInInitiationResult.Error("General error with CredentialManager: ${e.message}")
            }

        override suspend fun clearUserCredentials(onError: (Throwable) -> Unit) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
