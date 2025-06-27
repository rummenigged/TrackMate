package com.octopus.edu.feature.home

import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState

internal class HomeViewModel : BaseViewModel<UiState, UiEffect, UiEvent>() {
    override fun getInitialState(): UiState = UiState()

    override fun processEvent(event: UiEvent) {}
}
