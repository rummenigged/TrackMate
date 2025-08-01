package com.octopus.edu.core.ui.common.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface ViewState

interface ViewEffect

interface ViewEvent

abstract class BaseViewModel<UiState : ViewState, Effect : ViewEffect, Event : ViewEvent> : ViewModel() {
    private val _uiStateFlow by lazy { MutableStateFlow(getInitialState()) }

    val uiStateFlow
        get() = _uiStateFlow.asStateFlow()

    private val _effect = MutableStateFlow<Effect?>(value = null)
    val effect: Flow<Effect?>
        get() = _effect.asStateFlow()

    protected fun setState(reducer: UiState.() -> UiState) {
        _uiStateFlow.update(reducer)
    }

    protected fun setEffect(effect: Effect) {
        _effect.value = effect
    }

    abstract fun getInitialState(): UiState

    abstract fun processEvent(event: Event)
}
