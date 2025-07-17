package com.octopus.edu.trackmate.di

import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.workManager.HabitNotificationReminderScheduler
import com.octopus.edu.trackmate.workManager.TaskNotificationReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {
    @Binds
    @Singleton
    @Named("TaskNotificationReminderScheduler")
    abstract fun bindTaskNotificationReminderScheduler(reminderScheduler: TaskNotificationReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    @Named("HabitNotificationReminderScheduler")
    abstract fun bindHabitNotificationReminderScheduler(reminderScheduler: HabitNotificationReminderScheduler): ReminderScheduler
}
