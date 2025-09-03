package com.octopus.edu.feature.home.createEntry

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.octopus.edu.core.common.isToday
import com.octopus.edu.core.common.isTomorrow
import com.octopus.edu.core.common.isYesterday
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.models.EntryCreationData
import com.octopus.edu.feature.home.models.EntryDateState
import com.octopus.edu.feature.home.models.empty
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.time.ExperimentalTime

object AddEntryUiScreen {
    @Stable
    data class UiState(
        val isSetEntrySpecificationsModeEnabled: Boolean = false,
        val isSetEntryTimeModeEnabled: Boolean = false,
        val isSetEntryRecurrenceModeEnabled: Boolean = false,
        val isSetEntryReminderModeEnabled: Boolean = false,
        val isSetEntryReminderTypeModeEnabled: Boolean = false,
        val data: EntryCreationData = EntryCreationData.empty(),
        val dataDraftSnapshot: EntryCreationData = EntryCreationData.empty(),
    ) : ViewState {
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

        @get:StringRes
        val currentReminderTypeResolvedAsRes: Int
            get() =
                getReminderTypeAsStringRes(
                    dataDraftSnapshot.reminderType
                        ?: data.reminderType
                        ?: ReminderType.NOTIFICATION,
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

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
        ) : UiEffect

        data object ShowEntrySuccessfullyCreated : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        data object MarkEffectAsConsumed : UiEvent

        data object Save : UiEvent

        data object Cancel : UiEvent

        data object ShowSettingsPicker : UiEvent

        data object ConfirmDateAndTimeSettings : UiEvent

        data object CancelDateAndTimeSettings : UiEvent

        data object ShowTimePicker : UiEvent

        data object HideTimePicker : UiEvent

        data object ShowRecurrencePicker : UiEvent

        data object HideRecurrencePicker : UiEvent

        data object ShowReminderPicker : UiEvent

        data object ShowReminderTypePicker : UiEvent

        data object HideReminderPicker : UiEvent

        data object HideReminderTypePicker : UiEvent

        data class UpdateEntryTitle(
            val title: String,
        ) : UiEvent

        data class UpdateEntryDescription(
            val description: String,
        ) : UiEvent

        data class UpdateEntryDate(
            val date: LocalDate,
        ) : UiEvent

        data class UpdateEntryTime(
            val hour: Int,
            val minute: Int,
        ) : UiEvent

        data class UpdateEntryRecurrence(
            val recurrence: Recurrence
        ) : UiEvent

        data class UpdateEntryReminder(
            val reminder: Reminder
        ) : UiEvent

        data class UpdateEntryReminderType(
            val reminderType: ReminderType
        ) : UiEvent
    }

    internal fun UiState.Companion.emptyState(): UiState = UiState()

    internal fun UiState.emptySpecifications(): UiState =
        UiState(
            dataDraftSnapshot =
                dataDraftSnapshot.copy(
                    title = dataDraftSnapshot.title,
                    description = dataDraftSnapshot.description,
                ),
        )

    internal fun getReminderTypeAsStringRes(type: ReminderType): Int =
        when (type) {
            ReminderType.NOTIFICATION -> R.string.notification
            ReminderType.ALARM -> R.string.alarm
        }

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
    internal fun UiState.toDomain(): Entry =
        if (data.recurrence != null && data.recurrence !is Recurrence.None) {
            Habit(
                id = UUID.randomUUID().toString(),
                title = data.title ?: "",
                description = data.description ?: "",
                isDone = false,
                time = data.time,
                createdAt = Instant.now(),
                recurrence = data.recurrence,
                reminder = data.reminder,
                reminderType = data.reminderType,
                startDate = data.currentEntryDateOrToday,
            )
        } else {
            Task(
                id = UUID.randomUUID().toString(),
                title = data.title ?: "",
                description = data.description ?: "",
                isDone = false,
                time = data.time,
                dueDate = data.currentEntryDateOrToday,
                reminder = data.reminder,
                reminderType = data.reminderType,
                createdAt = Instant.now(),
            )
        }
}
