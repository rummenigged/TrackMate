package com.octopus.edu.feature.analytics

import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.analytics.AnalyticsUiContract.UiEffect
import com.octopus.edu.feature.analytics.AnalyticsUiContract.UiEvent
import com.octopus.edu.feature.analytics.AnalyticsUiContract.UiState

internal class AnalyticsViewModel : BaseViewModel<UiState, UiEffect, UiEvent>() {
    override fun getInitialState(): UiState = UiState(screenName = "Analytics Screen")

    override fun processEvent(event: UiEvent) {}
}
