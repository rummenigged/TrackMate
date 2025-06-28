package com.octopus.edu.feature.history

import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.history.HistoryUiContract.UiEffect
import com.octopus.edu.feature.history.HistoryUiContract.UiEvent
import com.octopus.edu.feature.history.HistoryUiContract.UiState

internal class HistoryViewModel : BaseViewModel<UiState, UiEffect, UiEvent>() {
    override fun getInitialState(): UiState = UiState(screenName = "History Screen")

    override fun processEvent(event: UiEvent) {}
}
