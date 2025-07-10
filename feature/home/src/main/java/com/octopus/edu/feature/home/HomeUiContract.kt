package com.octopus.edu.feature.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import com.octopus.edu.feature.home.HomeUiContract.EntryCreationState
import com.octopus.edu.feature.home.utils.isToday
import com.octopus.edu.feature.home.utils.isTomorrow
import com.octopus.edu.feature.home.utils.isYesterday
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.time.ExperimentalTime

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

internal object HomeUiContract {
    @Stable
    data class UiState(
        val entries: ImmutableList<Entry> = persistentListOf(),
        val isLoading: Boolean = false,
        val entryCreationState: EntryCreationState = EntryCreationState(),
    ) : ViewState

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
        ) : UiEffect

        data object ShowEntrySuccessfullyCreated : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        sealed interface Entry {
            data object Add : UiEvent

            data object Save : UiEvent
        }

        sealed interface AddEntry {
            data object Cancel : UiEvent

            data object ShowSettingsPicker : UiEvent

            data object ConfirmDateAndTimeSettings : UiEvent

            data object CancelDateAndTimeSettings : UiEvent

            data object ShowTimePicker : UiEvent

            data object HideTimePicker : UiEvent

            data object ShowRecurrencePicker : UiEvent

            data object HideRecurrencePicker : UiEvent
        }

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
    }

    @Stable
    internal data class EntryCreationState(
        val isEntryCreationModeEnabled: Boolean = false,
        val isSetEntryDateModeEnabled: Boolean = false,
        val isSetEntryTimeModeEnabled: Boolean = false,
        val isSetEntryRecurrenceModeEnabled: Boolean = false,
        val data: EntryCreationData = EntryCreationData(),
        val dataDraftSnapshot: EntryCreationData = EntryCreationData(),
    ) {
        val entryDateState: EntryDateState = getEntryDateAsFormattedText()

        val isDateBeforeToday: Boolean
            get() = data.currentEntryDate?.isBefore(LocalDate.now()) == true

        val isTimeBeforeNow: Boolean
            get() = data.currentEntryTime?.isBefore(LocalTime.now().minusMinutes(1)) == true

        private fun getEntryDateAsFormattedText(): EntryDateState =
            when {
                data.currentEntryDateOrToday.isToday() -> EntryDateState.Today(value = R.string.today)

                data.currentEntryDateOrToday.isTomorrow() -> EntryDateState.Tomorrow(value = R.string.tomorrow)

                this@EntryCreationState.data.currentEntryDateOrToday.isYesterday() -> EntryDateState.Yesterday(value = R.string.yesterday)

                data.currentEntryDateOrToday.isAfter(LocalDate.now()) -> {
                    val daysLeft = (data.currentEntryDateOrToday.toEpochDay() - LocalDate.now().toEpochDay()).toInt()
                    val month = data.currentEntryDateOrToday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val day = data.currentEntryDateOrToday.dayOfMonth.toString()
                    EntryDateState.DateLaterToday(
                        value = R.string.date,
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
                        value = R.string.date,
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

        data class EntryCreationData(
            val currentEntryTitle: String? = null,
            val currentEntryDescription: String? = null,
            val currentEntryDate: LocalDate? = null,
            val currentEntryTime: LocalTime? = null,
            val currentEntryReminder: String? = null,
            val currentEntryRecurrence: Recurrence? = null,
        ) {
            val currentEntryDateOrToday: LocalDate
                get() = currentEntryDate ?: LocalDate.now()
        }

        internal sealed class EntryDateState(
            @param:StringRes open val value: Int,
            open val time: LocalTime?,
            open val recurrence: Recurrence?,
        ) {
            data class Today(
                @param:StringRes override val value: Int,
                override val time: LocalTime? = null,
                override val recurrence: Recurrence? = null,
            ) : EntryDateState(value, time, recurrence)

            data class Tomorrow(
                @param:StringRes override val value: Int,
                override val time: LocalTime? = null,
                override val recurrence: Recurrence? = null,
            ) : EntryDateState(value, time, recurrence)

            data class Yesterday(
                @param:StringRes override val value: Int,
                override val time: LocalTime? = null,
                override val recurrence: Recurrence? = null,
            ) : EntryDateState(value, time, recurrence)

            data class DateLaterToday(
                @param:StringRes override val value: Int,
                override val time: LocalTime? = null,
                override val recurrence: Recurrence? = null,
                val daysLeft: Int,
                val month: String,
                val day: String
            ) : EntryDateState(value, time, recurrence)

            data class DateBeforeToday(
                @param:StringRes override val value: Int,
                override val time: LocalTime? = null,
                override val recurrence: Recurrence? = null,
                val daysOverdue: Int,
                val month: String,
                val day: String
            ) : EntryDateState(value, time, recurrence)
        }
    }
}

internal fun EntryCreationState.Companion.emptyState(): EntryCreationState = EntryCreationState()
