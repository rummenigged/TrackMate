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
        ) : UiEffect

        data object ShowEntrySuccessfullyDeleted : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        data object Refresh : UiEvent

        data class SetCurrentDateAs(
            val date: LocalDate
        ) : UiEvent

        sealed interface Entry {
            data class Delete(
                val entryId: String
            ) : UiEvent
        }
    }

    internal class UiEntry(
        val entry: Entry
    ) : Comparable<UiEntry> {
        override fun areItemsTheSame(newItem: UiEntry): Boolean = this.entry.id == newItem.entry.id

        override fun areContentsTheSame(newItem: UiEntry): Boolean = this.entry == newItem.entry
    }
}
