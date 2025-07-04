package com.octopus.edu.feature.home

import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import com.octopus.edu.feature.home.HomeUiContract.Tab.Habits
import com.octopus.edu.feature.home.HomeUiContract.Tab.Tasks
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal fun Habit.getRecurrenceAsText(): String? =
    when (recurrence) {
        Recurrence.Custom -> null
        Recurrence.Daily -> "Daily"
        Recurrence.Weekly -> "Weekly"
        null -> null
    }

internal object HomeUiContract {
    data class UiState(
        val tasks: ImmutableList<Task> = persistentListOf(),
        val habits: ImmutableList<Habit> = persistentListOf(),
        val tabSelected: Tab = Habits(),
        val isLoading: Boolean = false,
    ) : ViewState {
        val tabs: List<Tab> = listOf(Habits(), Tasks())

        val tabTitles: List<String>
            get() = tabs.map { it.title }

        fun getTab(position: Int): Tab = tabs[position]
    }

    sealed class Tab(
        open val title: String,
    ) {
        data class Habits(
            override val title: String = "Habit",
        ) : Tab(title)

        data class Tasks(
            override val title: String = "Tasks",
        ) : Tab(title)
    }

    sealed interface UiEffect : ViewEffect {
        data class ShowError(
            val message: String,
        ) : UiEffect
    }

    sealed interface UiEvent : ViewEvent {
        data class OnTabSelected(
            val tab: Tab,
        ) : UiEvent
    }
}
