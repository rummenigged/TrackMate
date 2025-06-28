package com.octopus.edu.feature.analytics

import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState

internal class AnalyticsUiContract {
    data class UiState(
        val screenName: String = "",
        val isLoading: Boolean = false,
    ) : ViewState

    sealed interface UiEffect : ViewEffect

    sealed interface UiEvent : ViewEvent
}
