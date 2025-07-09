package com.octopus.edu.feature.home

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.retryOnResultError
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalTime

@Suppress("ktlint:standard:indent")
@HiltViewModel
internal class HomeViewModel
@Inject
constructor(
    private val entryRepository: EntryRepository,
) : BaseViewModel<UiState, UiEffect, UiEvent>() {
    init {
        observeEntries()
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
                        entryCreationState = entryCreationState.copy(isEntryCreationModeEnabled = false),
                    )
                }

            UiEvent.AddEntry.ShowSettingsPicker ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(isSetEntryDateModeEnabled = true))
                }

            UiEvent.AddEntry.ConfirmDateAndTimeSettings ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(isSetEntryDateModeEnabled = false))
                }

            UiEvent.AddEntry.CancelDateAndTimeSettings -> {
                setState {
                    copy(entryCreationState = entryCreationState.copy(isSetEntryDateModeEnabled = false))
                }
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

            is UiEvent.UpdateEntryRecurrence ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(currentEntryRecurrence = event.recurrence))
                }

            is UiEvent.UpdateEntryDescription ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(currentEntryDescription = event.description))
                }

            is UiEvent.UpdateEntryTitle ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(currentEntryTitle = event.title))
                }

            is UiEvent.UpdateEntryDate ->
                setState {
                    copy(entryCreationState = entryCreationState.copy(currentEntryDate = event.date))
                }

            is UiEvent.UpdateEntryTime ->
                setState {
                    copy(
                        entryCreationState =
                            entryCreationState.copy(
                                isSetEntryTimeModeEnabled = false,
                                currentEntryTime = LocalTime.of(event.hour, event.minute),
                            ),
                    )
                }
        }
    }

    private fun observeEntries() =
        viewModelScope.launch {
            entryRepository
                .getEntriesOrderedByTime()
                .retryOnResultError(maxRetries = 2)
                .onStart { setState { copy(isLoading = true) } }
                .onEach { result ->
                    when (result) {
                        is ResultOperation.Error -> {
                            setEffect(UiEffect.ShowError(result.throwable.message ?: "Unknown Error"))
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
                            entryCreationState = HomeUiContract.EntryCreationState.emptyState(),
                        )
                    }
                    setEffect(UiEffect.ShowEntrySuccessfullyCreated)
                }
            }
        }
}
