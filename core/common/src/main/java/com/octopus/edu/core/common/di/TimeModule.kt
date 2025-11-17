package com.octopus.edu.core.common.di

import com.octopus.edu.core.common.AppClock
import com.octopus.edu.core.common.SystemAppClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideAppClock(): AppClock = SystemAppClock()
}
