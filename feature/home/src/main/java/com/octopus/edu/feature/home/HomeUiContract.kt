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
internal fun HomeUiContract.EntryCreationState.toDomain(): Entry =
    if (currentEntryRecurrence != null && currentEntryRecurrence !is Recurrence.None) {
        Habit(
            id = UUID.randomUUID().toString(),
            title = currentEntryTitle ?: "",
            description = currentEntryDescription ?: "",
            isDone = false,
            time = currentEntryTime,
            createdAt = Instant.now(),
            recurrence = currentEntryRecurrence,
        )
    } else {
        Task(
            id = UUID.randomUUID().toString(),
            title = currentEntryTitle ?: "",
            description = currentEntryDescription ?: "",
            isDone = false,
            time = currentEntryTime,
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
        val currentEntryTitle: String? = null,
        val currentEntryDescription: String? = null,
        val currentEntryDate: LocalDate? = null,
        val currentEntryTime: LocalTime? = null,
        val currentEntryReminder: String? = null,
        val currentEntryRecurrence: Recurrence? = null,
    ) {
        val currentEntryDateOrToday: LocalDate
            get() = currentEntryDate ?: LocalDate.now()

        val isEntrySettingsEdited: Boolean
            get() =
                currentEntryDate != null ||
                    currentEntryTime != null ||
                    currentEntryReminder != null ||
                    currentEntryRecurrence != null

        val entryDateState: EntryDateState = getEntryDateAsFormattedText()

        private fun getEntryDateAsFormattedText(): EntryDateState =
            when {
                currentEntryDateOrToday.isToday() -> EntryDateState.Today(value = R.string.today)

                currentEntryDateOrToday.isTomorrow() -> EntryDateState.Tomorrow(value = R.string.tomorrow)

                currentEntryDateOrToday.isYesterday() -> EntryDateState.Yesterday(value = R.string.yesterday)

                currentEntryDateOrToday.isAfter(LocalDate.now()) -> {
                    val daysLeft = (currentEntryDateOrToday.toEpochDay() - LocalDate.now().toEpochDay()).toInt()
                    val month = currentEntryDateOrToday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val day = currentEntryDateOrToday.dayOfMonth.toString()
                    EntryDateState.DaysLater(value = R.string.days_later, daysLeft = daysLeft, month = month, day = day)
                }

                else -> {
                    val daysOverdue = (LocalDate.now().toEpochDay() - currentEntryDateOrToday.toEpochDay()).toInt()
                    val month = currentEntryDateOrToday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val day = currentEntryDateOrToday.dayOfMonth.toString()
                    EntryDateState.DaysBefore(
                        value = R.string.days_before,
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

        internal sealed class EntryDateState(
            @param:StringRes open val value: Int
        ) {
            data class Today(
                @param:StringRes override val value: Int
            ) : EntryDateState(value)

            data class Tomorrow(
                @param:StringRes override val value: Int
            ) : EntryDateState(value)

            data class Yesterday(
                @param:StringRes override val value: Int
            ) : EntryDateState(value)

            data class DaysLater(
                @param:StringRes override val value: Int,
                val daysLeft: Int,
                val month: String,
                val day: String
            ) : EntryDateState(value)

            data class DaysBefore(
                @param:StringRes override val value: Int,
                val daysOverdue: Int,
                val month: String,
                val day: String
            ) : EntryDateState(value)
        }
    }
}

internal fun EntryCreationState.Companion.emptyState(): EntryCreationState = EntryCreationState()
