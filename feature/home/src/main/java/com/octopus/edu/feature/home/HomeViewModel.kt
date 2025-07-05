package com.octopus.edu.feature.home

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        init {
            getEntries()
        }

        override fun getInitialState(): UiState = UiState()

        override fun processEvent(event: UiEvent) {}

        private fun getEntries() =
            viewModelScope.launch {
                setState { copy(isLoading = true) }
                delay(3000)
                when (val result = entryRepository.getEntries()) {
                    is ResultOperation.Error -> {
                        setEffect(UiEffect.ShowError(result.exception.toString()))
                    }

                    is ResultOperation.Success -> {
                        setState {
                            copy(
                                entries = mockEntryList(5).toImmutableList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
    }
