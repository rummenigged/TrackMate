package com.octopus.edu.feature.home.createEntry

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.ReminderStrategyFactory
import com.octopus.edu.core.domain.scheduler.ReminderType.NOTIFICATION
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.UiEffect
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.UiEvent
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.UiState
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.emptyState
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.toDomain
import com.octopus.edu.feature.home.models.EntryCreationData
import com.octopus.edu.feature.home.models.empty
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.time.LocalTime

@HiltViewModel
class AddEntryViewModel
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        private val reminderStrategyFactory: ReminderStrategyFactory
    ) : BaseViewModel<UiState, UiEffect, UiEvent>() {
        override fun getInitialState(): UiState = UiState()

        override fun processEvent(event: UiEvent) {
            when (event) {
                UiEvent.Save -> {
                    saveCurrentEntry(entry = uiState.value.toDomain())
                }

                UiEvent.Cancel ->
                    setState {
                        copy(
                            isSetEntrySpecificationsModeEnabled = false,
                            dataDraftSnapshot = EntryCreationData.empty(),
                        )
                    }

                UiEvent.ShowSettingsPicker ->
                    setState {
                        copy(isSetEntrySpecificationsModeEnabled = true)
                    }

                UiEvent.ConfirmDateAndTimeSettings -> saveDateAndTimeSettings()

                UiEvent.CancelDateAndTimeSettings ->
                    setState {
                        copy(
                            isSetEntrySpecificationsModeEnabled = false,
                            dataDraftSnapshot = EntryCreationData.empty(),
                        )
                    }

                UiEvent.ShowTimePicker ->
                    setState {
                        copy(isSetEntryTimeModeEnabled = true)
                    }

                UiEvent.HideTimePicker ->
                    setState {
                        copy(isSetEntryTimeModeEnabled = false)
                    }

                UiEvent.ShowRecurrencePicker ->
                    setState {
                        copy(isSetEntryRecurrenceModeEnabled = true)
                    }

                UiEvent.HideRecurrencePicker ->
                    setState {
                        copy(isSetEntryRecurrenceModeEnabled = false)
                    }

                UiEvent.ShowReminderPicker ->
                    setState {
                        copy(isSetEntryReminderModeEnabled = true)
                    }

                UiEvent.ShowReminderTypePicker ->
                    setState {
                        copy(isSetEntryReminderTypeModeEnabled = true)
                    }

                UiEvent.HideReminderPicker ->
                    setState {
                        copy(isSetEntryReminderModeEnabled = false)
                    }

                UiEvent.HideReminderTypePicker ->
                    setState {
                        copy(isSetEntryReminderTypeModeEnabled = false)
                    }

                is UiEvent.UpdateEntryTitle ->
                    setState {
                        copy(
                            data =
                                data.copy(
                                    title = event.title,
                                ),
                        )
                    }

                is UiEvent.UpdateEntryDescription ->
                    setState {
                        copy(
                            data =
                                data.copy(
                                    description = event.description,
                                ),
                        )
                    }

                is UiEvent.UpdateEntryDate ->
                    setState {
                        copy(
                            dataDraftSnapshot =
                                dataDraftSnapshot.copy(
                                    date = event.date,
                                ),
                        )
                    }

                is UiEvent.UpdateEntryTime ->
                    setState {
                        copy(
                            isSetEntryTimeModeEnabled = false,
                            dataDraftSnapshot =
                                dataDraftSnapshot.copy(
                                    time = LocalTime.of(event.hour, event.minute),
                                ),
                        )
                    }

                is UiEvent.UpdateEntryRecurrence ->
                    setState {
                        copy(
                            isSetEntryRecurrenceModeEnabled = false,
                            dataDraftSnapshot =
                                dataDraftSnapshot.copy(
                                    recurrence = event.recurrence,
                                ),
                        )
                    }

                is UiEvent.UpdateEntryReminder ->
                    setState {
                        copy(
                            isSetEntryReminderModeEnabled = false,
                            dataDraftSnapshot =
                                dataDraftSnapshot.copy(
                                    reminder = event.reminder,
                                ),
                        )
                    }

                is UiEvent.UpdateEntryReminderType ->
                    setState {
                        copy(
                            isSetEntryReminderTypeModeEnabled = false,
                            dataDraftSnapshot =
                                dataDraftSnapshot.copy(
                                    reminderType = event.reminderType,
                                ),
                        )
                    }

                UiEvent.MarkEffectAsConsumed -> markEffectAsConsumed()
            }
        }

        private fun saveDateAndTimeSettings() {
            setState {
                copy(
                    isSetEntrySpecificationsModeEnabled = false,
                    data =
                        data.copy(
                            date =
                                dataDraftSnapshot.date
                                    ?: data.date,
                            time = dataDraftSnapshot.time,
                            reminder = dataDraftSnapshot.reminder,
                            reminderType = dataDraftSnapshot.reminderType,
                            recurrence = dataDraftSnapshot.recurrence,
                        ),
                )
            }
        }

        /**
             * Persists the given entry, clears the UI on success, schedules its reminder if present, and emits success or error effects.
             *
             * @param entry The entry to save; if it contains a reminder, that reminder will be scheduled after a successful save.
             */
            private fun saveCurrentEntry(entry: Entry) =
            viewModelScope.launch {
                when (val result = entryRepository.saveEntry(entry = entry)) {
                    is ResultOperation.Error -> {
                        setEffect(UiEffect.ShowError(result.throwable.toString()))
                    }

                    is ResultOperation.Success -> {
                        setState { UiState.emptyState() }

                        entry.reminder?.let { reminder ->
                            scheduleReminder(entry)
                        }

                        setEffect(UiEffect.EntrySuccessfullyCreated(R.string.entry_successfully_created))
                    }
                }
            }

        private fun scheduleReminder(entry: Entry) {
            reminderStrategyFactory
                .getStrategy(
                    entry,
                    entry.reminderType ?: NOTIFICATION,
                )?.schedule(entry)
        }

        fun clearAddEntrySpecificationsMode() {
            setState {
                copy(
                    isSetEntrySpecificationsModeEnabled = false,
                    isSetEntryTimeModeEnabled = false,
                    isSetEntryRecurrenceModeEnabled = false,
                    isSetEntryReminderModeEnabled = false,
                    isSetEntryReminderTypeModeEnabled = false,
                    dataDraftSnapshot = EntryCreationData.empty(),
                )
            }
        }
    }