package com.octopus.edu.feature.home

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal fun mockEntryList(count: Int): List<Entry> =
    (1..count).map {
        if (it % 2 == 0) {
            Task.mock(it.toString())
        } else {
            Habit.mock(it.toString())
        }
    }

internal fun Habit.getRecurrenceAsText(): String? =
    when (recurrence) {
        Recurrence.Custom -> null
        Recurrence.Daily -> "Daily"
        Recurrence.Weekly -> "Weekly"
        null -> null
    }

internal object HomeUiContract {
    data class UiState(
        val entries: ImmutableList<Entry> = persistentListOf(),
        val isLoading: Boolean = false,
    ) : ViewState

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
        ) : UiEffect
    }

    sealed interface UiEvent : ViewEvent
}
