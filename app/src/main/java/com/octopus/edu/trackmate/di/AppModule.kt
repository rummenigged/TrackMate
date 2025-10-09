package com.octopus.edu.trackmate.di

import com.octopus.edu.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @ApplicationScope
    @Provides
    fun providesApplicationScope(dispatcherProvider: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcherProvider.default)
}
