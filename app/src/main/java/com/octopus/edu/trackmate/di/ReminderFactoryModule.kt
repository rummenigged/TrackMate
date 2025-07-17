package com.octopus.edu.trackmate.di

import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import com.octopus.edu.core.domain.scheduler.ReminderStrategyFactory
import com.octopus.edu.trackmate.di.mapKey.ReminderStrategyMapKey
import com.octopus.edu.trackmate.reminder.DefaultReminderStrategyFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReminderFactoryModule {
    @Provides
    @Singleton
    fun provideReminderStrategyFactory(
        strategies: Map<@JvmSuppressWildcards ReminderStrategyMapKey, @JvmSuppressWildcards ReminderStrategy>
    ): ReminderStrategyFactory = DefaultReminderStrategyFactory(strategies)
}
