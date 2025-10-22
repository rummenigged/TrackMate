package com.octopus.edu.trackmate.di

import android.content.Context
import androidx.work.WorkManager
import com.octopus.edu.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
object AppModule {
    /**
         * Provides the application-wide CoroutineScope for launching top-level coroutines.
         *
         * The scope uses a SupervisorJob and the `default` dispatcher from [dispatcherProvider].
         *
         * @param dispatcherProvider Supplies the dispatchers; this scope uses its `default` dispatcher.
         * @return A `CoroutineScope` backed by a `SupervisorJob` and the provider's default dispatcher.
         */
        @Singleton
    @ApplicationScope
    @Provides
    fun providesApplicationScope(dispatcherProvider: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    /**
     * Provides the application-level WorkManager instance.
     *
     * @param context The application Context used to obtain the WorkManager.
     * @return The singleton WorkManager associated with the application context.
     */
    @Singleton
    @Provides
    fun providesWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}