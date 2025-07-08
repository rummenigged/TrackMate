package com.octopus.edu.feature.home

import androidx.compose.runtime.Stable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.LocalTime

internal fun getRecurrenceAsStringRes(recurrence: Recurrence): Int =
    when (recurrence) {
        Recurrence.Custom -> R.string.custom
        Recurrence.Daily -> R.string.daily
        Recurrence.Weekly -> R.string.weekly
        Recurrence.None -> R.string.none
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
    }

    sealed interface UiEvent : ViewEvent {
        sealed interface Entry {
            data object Add : UiEvent

            data object Save : UiEvent
        }

        sealed interface AddEntry {
            data object Cancel : UiEvent

            data object ShowSettingsPicker : UiEvent

            data object HideSettingsPicker : UiEvent

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
        val currentEntryDate: LocalDate = LocalDate.now(),
        val currentEntryTime: LocalTime? = null,
        val currentEntryReminder: String? = null,
        val currentEntryRecurrence: Recurrence? = null,
    ) {
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
}
