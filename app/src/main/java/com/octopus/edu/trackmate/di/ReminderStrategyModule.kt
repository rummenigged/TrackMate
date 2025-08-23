package com.octopus.edu.trackmate.di

import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.trackmate.di.mapKey.ReminderStrategyMapKey
import com.octopus.edu.trackmate.reminder.HabitAlarmReminderStrategy
import com.octopus.edu.trackmate.reminder.HabitNotificationReminderStrategy
import com.octopus.edu.trackmate.reminder.TaskAlarmReminderStrategy
import com.octopus.edu.trackmate.reminder.TaskNotificationReminderStrategy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object ReminderStrategyModule {
    @Suppress("ktlint:standard:max-line-length")
    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Task::class, type = ReminderType.NOTIFICATION)
    fun provideTaskNotificationReminderStrategy(
        @Named("TaskNotificationReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = TaskNotificationReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Habit::class, type = ReminderType.NOTIFICATION)
    fun provideHabitNotificationReminderStrategy(
        @Named("HabitNotificationReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = HabitNotificationReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Task::class, type = ReminderType.ALARM)
    fun provideTaskAlarmReminderStrategy(
        @Named("TaskAlarmReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = TaskAlarmReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Habit::class, type = ReminderType.ALARM)
    fun provideHabitAlarmReminderStrategy(
        @Named("HabitAlarmReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = HabitAlarmReminderStrategy(scheduler)
}
