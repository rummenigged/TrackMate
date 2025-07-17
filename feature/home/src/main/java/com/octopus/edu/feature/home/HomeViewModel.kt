package com.octopus.edu.feature.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.retryOnResultError
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.ReminderStrategyFactory
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import com.octopus.edu.feature.home.models.EntryCreationData
import com.octopus.edu.feature.home.models.EntryCreationState
import com.octopus.edu.feature.home.models.empty
import com.octopus.edu.feature.home.models.emptyState
import com.octopus.edu.feature.home.models.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        private val reminderStrategyFactory: ReminderStrategyFactory
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        init {
            getEntriesVisibleOn(getInitialState().currentDate)
        }

        override fun getInitialState(): UiState = UiState()

        override fun processEvent(event: UiEvent) {
            when (event) {
                UiEvent.Entry.Add ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isEntryCreationModeEnabled = true))
                    }

                UiEvent.Entry.Save -> {
                    saveCurrentEntry(entry = uiStateFlow.value.entryCreationState.toDomain())
                }

                UiEvent.AddEntry.Cancel ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    isEntryCreationModeEnabled = false,
                                    isSetEntryDateModeEnabled = false,
                                    dataDraftSnapshot = EntryCreationData.empty(),
                                ),
                        )
                    }

                UiEvent.AddEntry.ShowSettingsPicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryDateModeEnabled = true))
                    }

                UiEvent.AddEntry.ConfirmDateAndTimeSettings -> saveDateAndTimeSettings()

                UiEvent.AddEntry.CancelDateAndTimeSettings ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    isSetEntryDateModeEnabled = false,
                                    dataDraftSnapshot = EntryCreationData.empty(),
                                ),
                        )
                    }

                UiEvent.AddEntry.ShowTimePicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryTimeModeEnabled = true))
                    }

                UiEvent.AddEntry.HideTimePicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryTimeModeEnabled = false))
                    }

                UiEvent.AddEntry.ShowRecurrencePicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryRecurrenceModeEnabled = true))
                    }

                UiEvent.AddEntry.HideRecurrencePicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryRecurrenceModeEnabled = false))
                    }

                UiEvent.AddEntry.ShowReminderPicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryReminderModeEnabled = true))
                    }

                UiEvent.AddEntry.HideReminderPicker ->
                    setState {
                        copy(entryCreationState = entryCreationState.copy(isSetEntryReminderModeEnabled = false))
                    }

                is UiEvent.UpdateEntryTitle ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    data =
                                        entryCreationState.data.copy(
                                            title = event.title,
                                        ),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryDescription ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    data =
                                        entryCreationState.data.copy(
                                            description = event.description,
                                        ),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryDate ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    dataDraftSnapshot =
                                        entryCreationState.dataDraftSnapshot.copy(
                                            date = event.date,
                                        ),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryTime ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    isSetEntryTimeModeEnabled = false,
                                    dataDraftSnapshot =
                                        entryCreationState.dataDraftSnapshot.copy(
                                            time = LocalTime.of(event.hour, event.minute),
                                        ),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryRecurrence ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    isSetEntryRecurrenceModeEnabled = false,
                                    dataDraftSnapshot =
                                        entryCreationState.dataDraftSnapshot.copy(
                                            recurrence = event.recurrence,
                                        ),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryReminder ->
                    setState {
                        copy(
                            entryCreationState =
                                entryCreationState.copy(
                                    isSetEntryReminderModeEnabled = false,
                                    dataDraftSnapshot =
                                        entryCreationState.dataDraftSnapshot.copy(
                                            reminder = event.reminder,
                                        ),
                                ),
                        )
                    }

                is UiEvent.SelectCurrentDate -> getEntriesVisibleOn(event.date)
            }
        }

        private fun saveDateAndTimeSettings() {
            setState {
                copy(
                    entryCreationState =
                        entryCreationState.copy(
                            isSetEntryDateModeEnabled = false,
                            data =
                                entryCreationState.data.copy(
                                    date =
                                        entryCreationState.dataDraftSnapshot.date
                                            ?: entryCreationState.data.date,
                                    time = entryCreationState.dataDraftSnapshot.time,
                                    reminder = entryCreationState.dataDraftSnapshot.reminder,
                                    recurrence = entryCreationState.dataDraftSnapshot.recurrence,
                                ),
                        ),
                )
            }
        }

        private fun getEntriesVisibleOn(date: LocalDate) =
            viewModelScope.launch {
                entryRepository
                    .getEntriesVisibleOn(date)
                    .retryOnResultError(maxRetries = 2)
                    .onStart {
                        setState { copy(currentDate = date, isLoading = true) }
                    }.onEach { result ->
                        when (result) {
                            is ResultOperation.Error -> {
                                setEffect(
                                    UiEffect.ShowError(
                                        result.throwable.message
                                            ?: "Unknown Error",
                                    ),
                                )
                                Log.d("HomeViewModel", "saveCurrentEntry: ${result.throwable.message}")
                            }

                            is ResultOperation.Success -> {
                                setState {
                                    copy(
                                        isLoading = false,
                                        entries = result.data.toImmutableList(),
                                    )
                                }
                            }
                        }
                    }.collect()
            }

        private fun saveCurrentEntry(entry: Entry) =
            viewModelScope.launch {
                when (val result = entryRepository.saveEntry(entry = entry)) {
                    is ResultOperation.Error -> {
                        setEffect(UiEffect.ShowError(result.throwable.toString()))
                    }

                    is ResultOperation.Success -> {
                        setState {
                            copy(
                                entryCreationState = EntryCreationState.emptyState(),
                            )
                        }
                        entry.reminder?.let { reminder ->
                            scheduleReminder(entry)
                        }
                        setEffect(UiEffect.ShowEntrySuccessfullyCreated)
                    }
                }
            }

        private fun scheduleReminder(entry: Entry) {
            reminderStrategyFactory.getStrategy(entry, ReminderType.NOTIFICATION)?.schedule(entry)
        }
    }
