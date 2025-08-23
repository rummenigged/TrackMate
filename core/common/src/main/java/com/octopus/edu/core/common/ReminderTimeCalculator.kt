package com.octopus.edu.core.common

import com.octopus.edu.core.domain.model.Recurrence
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ReminderTimeCalculator {
    fun calculateReminderDelay(
        time: LocalTime,
        date: LocalDate,
        reminderOffset: Duration,
        now: LocalDateTime = LocalDateTime.now()
    ): Duration {
        val reminderDateTime = date.atTime(time).minus(reminderOffset)
        return Duration.between(now, reminderDateTime)
    }

    fun defaultTimeIfNull(time: LocalTime?): LocalTime = time ?: LocalTime.of(8, 0)

    fun getHabitInterval(
        recurrence: Recurrence?,
        reminderOffset: Duration
    ): Duration =
        when (recurrence) {
            Recurrence.Daily -> Duration.ofDays(1)
            Recurrence.Weekly -> Duration.ofDays(7)
            else -> Duration.ofDays(1)
        }.minus(reminderOffset)
}
