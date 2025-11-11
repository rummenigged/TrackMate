package com.octopus.edu.feature.home

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.di.ApplicationScope
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.retryOnResultError
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        @param:ApplicationScope private val applicationScope: CoroutineScope
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        private val markAsDoneConfirmationJobs = hashMapOf<String, Job>()

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

                is UiEvent.Entry.MarkAsDone ->
                    markEntryAsDone(event.entryId, uiState.value.currentDate, event.undoInterval)

                is UiEvent.Entry.UnmarkAsDone ->
                    unmarkEntryAsDone(event.entryId, uiState.value.currentDate)

                UiEvent.Refresh -> refreshData()

                UiEvent.MarkEffectAsConsumed -> markEffectAsConsumed()

                UiEvent.Entry.GetFromCurrentDate -> getEntriesVisibleOn(uiState.value.currentDate)
            }
        }

        private fun markEntryAsDone(
            entryId: String,
            entryDate: LocalDate,
            undoInterval: Long = 0L
        ) = viewModelScope.launch {
            when (val result = entryRepository.markEntryAsDone(entryId, entryDate)) {
                is ResultOperation.Error -> {
                    setEffect(
                        UiEffect.MarkEntryAsDoneFailed(
                            message = result.throwable.message ?: "Unknown Error",
                            isRetriable = result.isRetriable,
                            entryId = entryId,
                        ),
                    )
                }
                is ResultOperation.Success -> {
                    setEffect(
                        UiEffect.ShowEntrySuccessfullyMarkedAsDone(entryId),
                    )
                    scheduleMarkingEntryAsDoneConfirmation(entryId, entryDate, undoInterval)
                }
            }
        }

        private fun scheduleMarkingEntryAsDoneConfirmation(
            entryId: String,
            entryDate: LocalDate,
            delay: Long
        ) = applicationScope.launch {
            markAsDoneConfirmationJobs[entryId] =
                launch {
                    delay(delay)
                    when (val result = entryRepository.confirmEntryAsDone(entryId, entryDate)) {
                        is ResultOperation.Error -> {
                            Logger.e(
                                message = "Can't confirm entry $entryId as Done on date ${"$entryDate(${entryDate.toEpochMilli()})"}.",
                                throwable = result.throwable,
                            )
                        }
                        is ResultOperation.Success -> {}
                    }
                }
        }

        private fun unmarkEntryAsDone(
            entryId: String,
            entryDate: LocalDate
        ) = viewModelScope.launch {
            markAsDoneConfirmationJobs[entryId]?.cancel()
            when (val result = entryRepository.unmarkEntryAsDone(entryId, entryDate)) {
                is ResultOperation.Error -> {
                    setEffect(
                        UiEffect.UnmarkEntryAsDoneFailed(
                            message = result.throwable.message ?: "Unknown Error",
                        ),
                    )
                    Logger.e(
                        message = "Can't mark entry $entryId as Undone.",
                        throwable = result.throwable,
                    )
                }
                is ResultOperation.Success -> {}
            }
        }

        private fun refreshData() {
            viewModelScope.launch {
                setState { copy(isRefreshing = true) }
                when (val result = entryRepository.syncEntries()) {
                    is ResultOperation.Error -> {
                        setState { copy(isRefreshing = false) }
                        setEffect(
                            UiEffect.ShowError(
                                result.throwable.message
                                    ?: "Unknown Error",
                            ),
                        )
                    }
                    is ResultOperation.Success -> {
                        setState { copy(isRefreshing = false) }
                    }
                }
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
                    }.collectLatest { result ->
                        when (result) {
                            is ResultOperation.Error -> {
                                setState { copy(isLoading = false) }
                                setEffect(
                                    UiEffect.ShowError(
                                        result.throwable.message
                                            ?: "Unknown Error",
                                    ),
                                )
                                Logger.e(
                                    message = result.throwable.message ?: "Unknown Error",
                                    throwable = result.throwable,
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
                    }
            }
    }
