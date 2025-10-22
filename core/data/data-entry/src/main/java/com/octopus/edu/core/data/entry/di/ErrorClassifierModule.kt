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
    /**
     * Provides an ErrorClassifier implementation for classifying database-related errors.
     *
     * @return An ErrorClassifier backed by DatabaseErrorClassifier.
     */
    @DatabaseErrorClassifierQualifier
    @Provides
    fun provideDatabaseErrorClassifier(): ErrorClassifier = DatabaseErrorClassifier()

    /**
     * Provides an ErrorClassifier that classifies network-related errors.
     *
     * @return An ErrorClassifier implementation for classifying network errors.
     */
    @NetworkErrorClassifierQualifier
    @Provides
    fun provideNetworkErrorClassifier(): ErrorClassifier = NetworkErrorClassifier()

    /**
     * Provides an ErrorClassifier that classifies synchronization errors by combining database and network classifiers.
     *
     * @param databaseErrorClassifier Classifier used for database-related errors.
     * @param networkErrorClassifier Classifier used for network-related errors.
     * @return An ErrorClassifier that delegates sync error classification to the provided database and network classifiers.
     */
    @SyncErrorClassifierQualifier
    @Provides
    fun provideSyncErrorClassifier(
        @DatabaseErrorClassifierQualifier databaseErrorClassifier: ErrorClassifier,
        @NetworkErrorClassifierQualifier networkErrorClassifier: ErrorClassifier
    ): ErrorClassifier = SyncErrorClassifier(databaseErrorClassifier, networkErrorClassifier)
}