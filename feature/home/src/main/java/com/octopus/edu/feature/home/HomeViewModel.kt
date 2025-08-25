package com.octopus.edu.feature.home

import androidx.lifecycle.viewModelScope
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
import java.time.LocalDate

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val entryRepository: EntryRepository
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        init {
            getEntriesVisibleOn(getInitialState().currentDate)
        }

        override fun getInitialState(): UiState = UiState()

        override fun processEvent(event: UiEvent) {
            when (event) {
                is UiEvent.Entry.Delete -> {
                    deleteEntry(event.entryId)
                }

                is UiEvent.SetCurrentDateAs -> getEntriesVisibleOn(event.date)
            }
        }

        private fun deleteEntry(entryId: String) =
            viewModelScope.launch {
                setState { copy(isLoading = true) }
                when (val result = entryRepository.deleteEntry(entryId)) {
                    is ResultOperation.Error -> {
                        setEffect(
                            UiEffect.ShowError(
                                result.throwable.message
                                    ?: "Unknown Error",
                            ),
                        )
                    }

                    is ResultOperation.Success -> {
                        setState { copy(isLoading = false) }
                        setEffect(UiEffect.ShowEntrySuccessfullyDeleted)
                    }
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
    }
