package com.octopus.edu.feature.home.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
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

@OptIn(ExperimentalTime::class)
internal fun EntryCreationState.toDomain(): Entry =
    if (data.currentEntryRecurrence != null && data.currentEntryRecurrence !is Recurrence.None) {
        Habit(
            id = UUID.randomUUID().toString(),
            title = data.currentEntryTitle ?: "",
            description = data.currentEntryDescription ?: "",
            isDone = false,
            time = data.currentEntryTime,
            createdAt = Instant.now(),
            recurrence = data.currentEntryRecurrence,
        )
    } else {
        Task(
            id = UUID.randomUUID().toString(),
            title = data.currentEntryTitle ?: "",
            description = data.currentEntryDescription ?: "",
            isDone = false,
            time = data.currentEntryTime,
            createdAt = Instant.now(),
        )
    }

@Stable
internal data class EntryCreationState(
    val isEntryCreationModeEnabled: Boolean = false,
    val isSetEntryDateModeEnabled: Boolean = false,
    val isSetEntryTimeModeEnabled: Boolean = false,
    val isSetEntryRecurrenceModeEnabled: Boolean = false,
    val data: EntryCreationData = EntryCreationData.empty(),
    val dataDraftSnapshot: EntryCreationData = EntryCreationData.empty(),
) {
    val entryDateState: EntryDateState = getCurrentEntryDateState()

    val isDateBeforeToday: Boolean
        get() = data.currentEntryDate?.isBefore(LocalDate.now()) == true

    val isTimeBeforeNow: Boolean
        get() = data.currentEntryTime?.isBefore(LocalTime.now().minusMinutes(1)) == true

    val currentTimeResolvedAsText: String?
        get() = dataDraftSnapshot.currentEntryTime?.toString() ?: data.currentEntryTime?.toString()

    val isTimeFilled: Boolean
        get() = dataDraftSnapshot.currentEntryTime != null || data.currentEntryTime != null

    val isOverdue: Boolean
        get() =
            isDateBeforeToday ||
                (entryDateState is EntryDateState.Today && isTimeBeforeNow)

    @get:StringRes
    val currentRecurrenceResolvedAsRes: Int
        get() =
            getRecurrenceAsStringRes(
                dataDraftSnapshot.currentEntryRecurrence
                    ?: data.currentEntryRecurrence
                    ?: Recurrence.None,
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
    }
}
