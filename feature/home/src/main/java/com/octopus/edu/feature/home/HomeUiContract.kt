package com.octopus.edu.feature.home

import androidx.compose.runtime.Stable
import com.octopus.edu.core.design.theme.utils.Comparable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import com.octopus.edu.feature.home.models.EntryCreationState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

internal object HomeUiContract {
    @Stable
    data class UiState(
        val entries: ImmutableList<Entry> = persistentListOf(),
        val currentDate: LocalDate = LocalDate.now(),
        val isLoading: Boolean = false,
        val entryCreationState: EntryCreationState = EntryCreationState(),
    ) : ViewState {
        val currentMonth: String
            get() = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val currentYear: String
            get() = currentDate.year.toString()
    }

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
        ) : UiEffect

        data object ShowEntrySuccessfullyCreated : UiEffect

        data object ShowEntrySuccessfullyDeleted : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        data class SetCurrentDateAs(
            val date: LocalDate
        ) : UiEvent

        sealed interface Entry {
            data object Add : UiEvent

            data object Save : UiEvent

            data class Delete(
                val entryId: String
            ) : UiEvent
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

            data object ShowReminderPicker : UiEvent

            data object HideReminderPicker : UiEvent
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

        data class UpdateEntryReminder(
            val reminder: Reminder
        ) : UiEvent
    }

    internal class UiEntry(
        val entry: Entry
    ) : Comparable<UiEntry> {
        override fun areItemsTheSame(newItem: UiEntry): Boolean = this.entry.id == newItem.entry.id

        override fun areContentsTheSame(newItem: UiEntry): Boolean = this.entry == newItem.entry
    }
}
