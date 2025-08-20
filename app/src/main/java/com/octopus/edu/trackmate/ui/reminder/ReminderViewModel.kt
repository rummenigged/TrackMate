package com.octopus.edu.trackmate.ui.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.trackmate.reminderSchedulers.TaskAlarmReminderScheduler.Companion.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEffect
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEvent
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
internal class ReminderViewModel
    @Inject
    constructor(
        savedStateHandler: SavedStateHandle,
        private val entryRepository: EntryRepository
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        override fun getInitialState(): UiState = UiState()

        init {
            savedStateHandler.get<String>(ENTRY_ID_EXTRA)?.let { entryId ->
                getReminder(entryId)
            }
        }

        override fun processEvent(event: UiEvent) =
            when (event) {
                UiEvent.OnDismiss -> UiEffect.Dismiss.send()
                UiEvent.MarkEffectAsConsumed -> markEffectAsConsumed()
            }

        private fun getReminder(entryId: String) =
            viewModelScope.launch {
                when (val result = entryRepository.getEntryById(entryId)) {
                    is ResultOperation.Error -> {
                        UiEffect.ShowError.send()
                    }
                    is ResultOperation.Success -> {
                        with(result.data) {
                            val date =
                                when (this) {
                                    is Habit -> LocalDateTime.of(this.startDate, this.time)
                                    is Task -> LocalDateTime.of(this.dueDate, this.time)
                                }
                            setState {
                                copy(
                                    entry = title,
                                    date = date,
                                    reminder = reminder,
                                )
                            }
                        }
                    }
                }
            }
    }
