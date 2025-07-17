package com.octopus.edu.core.common.di

import com.octopus.edu.core.common.CoroutineDispatcherProvider
import com.octopus.edu.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = CoroutineDispatcherProvider()
}
