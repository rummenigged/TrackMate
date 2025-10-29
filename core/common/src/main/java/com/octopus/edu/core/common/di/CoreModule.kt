package com.octopus.edu.core.common.di

import com.octopus.edu.core.common.CoroutineDispatcherProvider
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.ExponentialBackoffPolicy
import com.octopus.edu.core.common.ExponentialBackoffPolicy.Companion.DEFAULT_INITIAL_DELAY
import com.octopus.edu.core.common.ExponentialBackoffPolicy.Companion.DEFAULT_MAX_DELAY
import com.octopus.edu.core.common.RetryPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = CoroutineDispatcherProvider()

    @Provides
    fun provideRetryPolicy(): RetryPolicy =
        ExponentialBackoffPolicy(
            initialDelay = DEFAULT_INITIAL_DELAY,
            maxDelay = DEFAULT_MAX_DELAY,
        )
}
