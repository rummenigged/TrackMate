package com.octopus.edu.core.common.di

import android.app.Application
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.octopus.edu.core.common.credentialService.AndroidCredentialManagerService
import com.octopus.edu.core.domain.credentialManager.ICredentialService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class CredentialServiceModule {
    @Provides
    fun provideGoogleCredentialOption(
        @Named("server_client_id") serverClientId: String
    ): GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()

    @Provides
    fun provideCredentialRequest(option: GetSignInWithGoogleOption): GetCredentialRequest =
        GetCredentialRequest.Builder().addCredentialOption(option).build()

    @Provides
    fun provideCredentialManager(application: Application): CredentialManager = CredentialManager.create(application)

    /**
         * Creates an ICredentialService backed by AndroidCredentialManagerService.
         *
         * @param credentialRequest The GetCredentialRequest containing sign-in options to use.
         * @param credentialManager The CredentialManager used to perform credential operations.
         * @return An ICredentialService implementation that uses the provided request and manager.
         */
        @Provides
    fun provideCredentialService(
        credentialRequest: GetCredentialRequest,
        credentialManager: CredentialManager
    ): ICredentialService =
        AndroidCredentialManagerService(
            credentialRequest = credentialRequest,
            credentialManager = credentialManager,
        )
}