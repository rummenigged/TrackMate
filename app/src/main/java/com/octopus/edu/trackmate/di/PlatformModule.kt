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

    /**
     * Binds the HabitAlarmReminderScheduler as the ReminderScheduler with the "HabitAlarmReminderScheduler" qualifier.
     *
     * @param reminderScheduler The HabitAlarmReminderScheduler instance to bind.
     * @return The bound ReminderScheduler implementation.
     */
    @Binds
    @Singleton
    @Named("HabitAlarmReminderScheduler")
    fun bindHabitAlarmReminderScheduler(reminderScheduler: HabitAlarmReminderScheduler): ReminderScheduler

    /**
     * Binds an EntrySyncWorkScheduler as the EntrySyncScheduler implementation in the DI graph.
     *
     * @param entrySyncWorkScheduler The concrete EntrySyncWorkScheduler to provide where EntrySyncScheduler is required.
     * @return The EntrySyncScheduler bound to the provided implementation.
     */
    @Binds
    @Singleton
    fun bindEntrySyncScheduler(entrySyncWorkScheduler: EntrySyncWorkScheduler): EntrySyncScheduler
}