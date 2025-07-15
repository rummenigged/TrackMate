package com.octopus.edu.feature.home.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.utils.isToday
import com.octopus.edu.feature.home.utils.isTomorrow
import com.octopus.edu.feature.home.utils.isYesterday
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.time.ExperimentalTime

internal fun EntryCreationState.Companion.emptyState(): EntryCreationState = EntryCreationState()

internal fun getRecurrenceAsStringRes(recurrence: Recurrence): Int =
    when (recurrence) {
        Recurrence.Custom -> R.string.custom
        Recurrence.Daily -> R.string.daily
        Recurrence.Weekly -> R.string.weekly
        Recurrence.None -> R.string.none
    }

internal fun getReminderAsStringRes(reminder: Reminder): Int =
    when (reminder) {
        Reminder.DayEarly -> R.string.day_early
        Reminder.FiveMinutesEarly -> R.string.five_minutes_early
        Reminder.OnDay -> R.string.on_day
        Reminder.OnTime -> R.string.on_time
        Reminder.OneHourEarly -> R.string.one_hour_early
        Reminder.ThirtyMinutesEarly -> R.string.thirty_minutes_early
        Reminder.ThreeDaysEarly -> R.string.three_days_early
        Reminder.TwoDaysEarly -> R.string.two_days_early
        Reminder.None -> R.string.none
    }

@OptIn(ExperimentalTime::class)
internal fun EntryCreationState.toDomain(): Entry =
    if (data.recurrence != null && data.recurrence !is Recurrence.None) {
        Habit(
            id = UUID.randomUUID().toString(),
            title = data.title ?: "",
            description = data.description ?: "",
            isDone = false,
            time = data.time,
            createdAt = Instant.now(),
            recurrence = data.recurrence,
        )
    } else {
        Task(
            id = UUID.randomUUID().toString(),
            title = data.title ?: "",
            description = data.description ?: "",
            isDone = false,
            time = data.time,
            createdAt = Instant.now(),
        )
    }

@Stable
internal data class EntryCreationState(
    val isEntryCreationModeEnabled: Boolean = false,
    val isSetEntryDateModeEnabled: Boolean = false,
    val isSetEntryTimeModeEnabled: Boolean = false,
    val isSetEntryRecurrenceModeEnabled: Boolean = false,
    val isSetEntryReminderModeEnabled: Boolean = false,
    val data: EntryCreationData = EntryCreationData.empty(),
    val dataDraftSnapshot: EntryCreationData = EntryCreationData.empty(),
) {
    val entryDateState: EntryDateState = getCurrentEntryDateState()

    val isDateBeforeToday: Boolean
        get() = data.date?.isBefore(LocalDate.now()) == true

    val isTimeBeforeNow: Boolean
        get() = data.time?.isBefore(LocalTime.now().minusMinutes(1)) == true

    val currentTimeResolvedAsText: String?
        get() = dataDraftSnapshot.time?.toString() ?: data.time?.toString()

    val isTimeFilled: Boolean
        get() = dataDraftSnapshot.time != null || data.time != null

    val isOverdue: Boolean
        get() =
            isDateBeforeToday ||
                (entryDateState is EntryDateState.Today && isTimeBeforeNow)

    @get:StringRes
    val currentRecurrenceResolvedAsRes: Int
        get() =
            getRecurrenceAsStringRes(
                dataDraftSnapshot.recurrence
                    ?: data.recurrence
                    ?: Recurrence.None,
            )

    @get:StringRes
    val currentReminderResolvedAsRes: Int
        get() =
            getReminderAsStringRes(
                dataDraftSnapshot.reminder
                    ?: data.reminder
                    ?: Reminder.None,
            )

    private fun getCurrentEntryDateState(): EntryDateState =
        when {
            data.currentEntryDateOrToday.isToday() -> EntryDateState.Today()

            data.currentEntryDateOrToday.isTomorrow() -> EntryDateState.Tomorrow()

            data.currentEntryDateOrToday.isYesterday() -> EntryDateState.Yesterday()

            data.currentEntryDateOrToday.isAfter(LocalDate.now()) -> {
                val daysLeft = (data.currentEntryDateOrToday.toEpochDay() - LocalDate.now().toEpochDay()).toInt()
                val month = data.currentEntryDateOrToday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val day = data.currentEntryDateOrToday.dayOfMonth.toString()
                EntryDateState.DateLaterToday(
                    daysLeft = daysLeft,
                    month = month,
                    day = day,
                )
            }

            else -> {
                val daysOverdue = (LocalDate.now().toEpochDay() - data.currentEntryDateOrToday.toEpochDay()).toInt()
                val month = data.currentEntryDateOrToday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val day = data.currentEntryDateOrToday.dayOfMonth.toString()
                EntryDateState.DateBeforeToday(
                    daysOverdue = daysOverdue,
                    month = month,
                    day = day,
                )
            }
        }

    companion object {
        val recurrenceOptions
            get() =
                listOf(
                    Recurrence.None,
                    Recurrence.Daily,
                    Recurrence.Weekly,
                    Recurrence.Custom,
                ).toImmutableList()

        val reminderByDayOptions
            get() =
                listOf(
                    Reminder.None,
                    Reminder.OnDay,
                    Reminder.DayEarly,
                    Reminder.TwoDaysEarly,
                    Reminder.ThreeDaysEarly,
                ).toImmutableList()

        val reminderByTimeOptions
            get() =
                listOf(
                    Reminder.None,
                    Reminder.OnTime,
                    Reminder.FiveMinutesEarly,
                    Reminder.ThirtyMinutesEarly,
                    Reminder.OneHourEarly,
                ).toImmutableList()
    }
}
