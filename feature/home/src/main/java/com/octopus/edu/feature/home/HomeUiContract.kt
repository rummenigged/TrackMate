package com.octopus.edu.feature.home

import androidx.compose.runtime.Stable
import com.octopus.edu.core.design.theme.utils.Comparable
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
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
        val isRefreshing: Boolean = false
    ) : ViewState {
        val currentMonth: String
            get() = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val currentYear: String
            get() = currentDate.year.toString()
    }

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
            val isRetriable: Boolean = false
        ) : UiEffect

        data class MarkEntryAsDoneFailed(
            val entryId: String,
            val message: String,
            val isRetriable: Boolean = false
        ) : UiEffect

        data class UnmarkEntryAsDoneFailed(
            val message: String,
        ) : UiEffect

        data object ShowEntrySuccessfullyDeleted : UiEffect

        data class ShowEntrySuccessfullyMarkedAsDone(
            val entryId: String
        ) : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        data object Refresh : UiEvent

        data class SetCurrentDateAs(
            val date: LocalDate
        ) : UiEvent

        data object MarkEffectAsConsumed : UiEvent

        sealed interface Entry {
            data class Delete(
                val entryId: String
            ) : UiEvent

            data class MarkAsDone(
                val entryId: String,
                val undoInterval: Long
            ) : UiEvent

            data class UnmarkAsDone(
                val entryId: String
            ) : UiEvent

            data object GetFromCurrentDate : UiEvent
        }
    }

    internal class UiEntry(
        val entry: Entry
    ) : Comparable<UiEntry> {
        override fun areItemsTheSame(newItem: UiEntry): Boolean = this.entry.id == newItem.entry.id

        override fun areContentsTheSame(newItem: UiEntry): Boolean = this.entry == newItem.entry
    }
}
