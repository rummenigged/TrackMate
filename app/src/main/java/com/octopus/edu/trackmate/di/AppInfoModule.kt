package com.octopus.edu.trackmate.di

import android.content.Context
import com.octopus.edu.trackmate.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class AppInfoModule {
    @Provides
    @Named("server_client_id")
    fun providerServerClientId(
        @ApplicationContext context: Context
    ): String = context.getString(R.string.default_web_client_id)
}
