package com.octopus.edu.trackmate.di

import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.workManager.ReminderSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {
    @Binds
    @Singleton
    abstract fun bindReminderScheduler(reminderScheduler: ReminderSchedulerImpl): ReminderScheduler
}
