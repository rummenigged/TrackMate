package com.octopus.edu.feature.home

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.mockList
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.Tab.Habits
import com.octopus.edu.feature.home.HomeUiContract.Tab.Tasks
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
        override fun getInitialState(): UiState = UiState()

        override fun processEvent(event: UiEvent) {
            when (event) {
                is UiEvent.OnTabSelected -> {
                    when (event.tab) {
                        is Habits -> getHabits()
                        is Tasks -> getTasks()
                    }
                }
            }
        }

        private fun getHabits() =
            viewModelScope.launch {
                setState { copy(isLoading = true, tabSelected = Habits()) }
                delay(3000)
                when (val result = entryRepository.getHabits()) {
                    is ResultOperation.Error -> {
                        setEffect(UiEffect.ShowError(result.exception.toString()))
                    }

                    is ResultOperation.Success -> {
                        setState {
                            copy(
                                habits = Habit.mockList(5).toImmutableList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }

        private fun getTasks() =
            viewModelScope.launch {
                setState { copy(isLoading = true, tabSelected = Tasks()) }
                delay(3000)
                when (val result = entryRepository.getTasks()) {
                    is ResultOperation.Error -> setEffect(UiEffect.ShowError(result.exception.toString()))
                    is ResultOperation.Success -> {
                        setState {
                            copy(
                                tasks = Task.mockList(5).toImmutableList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
    }
