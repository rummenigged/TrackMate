package com.octopus.edu.core.ui.common.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface ViewState

interface ViewEffect

interface ViewEvent

abstract class BaseViewModel<UiState : ViewState, Effect : ViewEffect, Event : ViewEvent> : ViewModel() {
    private val initialState: UiState by lazy { getInitialState() }

    private var _viewState = mutableStateOf(initialState)

    var viewState by _viewState
        private set

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
