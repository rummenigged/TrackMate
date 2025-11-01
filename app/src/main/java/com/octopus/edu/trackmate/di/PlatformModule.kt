package com.octopus.edu.trackmate.di

import com.octopus.edu.core.domain.scheduler.EntrySyncScheduler
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.reminderSchedulers.HabitAlarmReminderScheduler
import com.octopus.edu.trackmate.reminderSchedulers.HabitNotificationReminderScheduler
import com.octopus.edu.trackmate.reminderSchedulers.TaskAlarmReminderScheduler
import com.octopus.edu.trackmate.reminderSchedulers.TaskNotificationReminderScheduler
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PlatformModule {
    @Binds
    @Singleton
    @Named("TaskNotificationReminderScheduler")
    fun bindTaskNotificationReminderScheduler(reminderScheduler: TaskNotificationReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    @Named("HabitNotificationReminderScheduler")
    fun bindHabitNotificationReminderScheduler(reminderScheduler: HabitNotificationReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    @Named("TaskAlarmReminderScheduler")
    fun bindTaskAlarmReminderScheduler(reminderScheduler: TaskAlarmReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    @Named("HabitAlarmReminderScheduler")
    fun bindHabitAlarmReminderScheduler(reminderScheduler: HabitAlarmReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    fun bindEntrySyncScheduler(entrySyncWorkScheduler: EntrySyncWorkScheduler): EntrySyncScheduler
}
