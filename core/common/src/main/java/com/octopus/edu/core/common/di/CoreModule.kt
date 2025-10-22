package com.octopus.edu.core.common.di

import com.octopus.edu.core.common.CoroutineDispatcherProvider
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.ExponentialBackoffPolicy
import com.octopus.edu.core.common.ExponentialBackoffPolicy.Companion.DEFAULT_INITIAL_DELAY
import com.octopus.edu.core.common.ExponentialBackoffPolicy.Companion.DEFAULT_MAX_DELAY
import com.octopus.edu.core.domain.utils.RetryPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    /**
     * Provides a DispatcherProvider that exposes standard coroutine dispatchers.
     *
     * @return A DispatcherProvider supplying Default, IO, Main and Unconfined coroutine dispatchers.
     */
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = CoroutineDispatcherProvider()

    /**
         * Creates a RetryPolicy configured to use exponential backoff with the module's default delays.
         *
         * @return A `RetryPolicy` that uses `DEFAULT_INITIAL_DELAY` as the initial delay and
         * `DEFAULT_MAX_DELAY` as the maximum delay.
         */
        @Provides
    fun provideRetryPolice(): RetryPolicy =
        ExponentialBackoffPolicy(
            initialDelay = DEFAULT_INITIAL_DELAY,
            maxDelay = DEFAULT_MAX_DELAY,
        )
}