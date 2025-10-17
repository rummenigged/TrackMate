package com.octopus.edu.core.data.entry.di

import com.octopus.edu.core.data.entry.DatabaseErrorClassifier
import com.octopus.edu.core.data.entry.NetworkErrorClassifier
import com.octopus.edu.core.data.entry.SyncErrorClassifier
import com.octopus.edu.core.domain.utils.ErrorClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SyncErrorClassifierQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DatabaseErrorClassifierQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkErrorClassifierQualifier

@Module
@InstallIn(SingletonComponent::class)
object ErrorClassifierModule {
    @DatabaseErrorClassifierQualifier
    @Provides
    fun provideDatabaseErrorClassifier(): ErrorClassifier = DatabaseErrorClassifier()

    @NetworkErrorClassifierQualifier
    @Provides
    fun provideNetworkErrorClassifier(): ErrorClassifier = NetworkErrorClassifier()

    @SyncErrorClassifierQualifier
    @Provides
    fun provideSyncErrorClassifier(
        @DatabaseErrorClassifierQualifier databaseErrorClassifier: ErrorClassifier,
        @NetworkErrorClassifierQualifier networkErrorClassifier: ErrorClassifier
    ): ErrorClassifier = SyncErrorClassifier(databaseErrorClassifier, networkErrorClassifier)
}
