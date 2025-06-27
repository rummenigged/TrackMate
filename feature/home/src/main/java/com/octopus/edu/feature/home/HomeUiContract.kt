package com.octopus.edu.feature.home

import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState

internal class HomeUiContract {
    data class UiState(
        val isLoading: Boolean = false,
    ) : ViewState

    sealed interface UiEffect : ViewEffect

    sealed interface UiEvent : ViewEvent
}
