# BaseViewModel Pattern

This document describes the `BaseViewModel` implementation and how to use it effectively in TrackMate.

## Overview

`BaseViewModel` is an abstract class that implements the MVI (Model-View-Intent) pattern, providing:

- Single source of truth for UI state via `StateFlow`
- One-shot side effects via nullable `Effect` flow
- Type-safe event handling through `processEvent()`

## Implementation

### Location

`core/ui-common/src/main/java/com/octopus/edu/core/ui/common/base/BaseViewModel.kt`

### Source Code

```kotlin
interface ViewState
interface ViewEffect
interface ViewEvent

abstract class BaseViewModel<UiState : ViewState, Effect : ViewEffect, Event : ViewEvent> : ViewModel() {

    private val _uiState by lazy { MutableStateFlow(getInitialState()) }
    val uiState get() = _uiState.asStateFlow()

    private val _effect = MutableStateFlow<Effect?>(value = null)
    val effect: Flow<Effect?> get() = _effect.asStateFlow()

    protected fun setState(reducer: UiState.() -> UiState) {
        _uiState.update(reducer)
    }

    protected fun setEffect(effect: Effect) {
        _effect.value = effect
    }

    protected fun markEffectAsConsumed() {
        _effect.value = null
    }

    abstract fun getInitialState(): UiState
    abstract fun processEvent(event: Event)

    protected fun Effect.send() = setEffect(this)
}
```

## Key Concepts

### 1. Type Parameters

| Parameter | Purpose | Marker Interface |
|-----------|---------|------------------|
| `UiState` | Screen state (what to render) | `ViewState` |
| `Effect` | One-shot side effects | `ViewEffect` |
| `Event` | User interactions | `ViewEvent` |

### 2. State Management

State is managed through a reducer function that receives the current state:

```kotlin
// Single field update
setState { copy(isLoading = true) }

// Multiple field update
setState {
    copy(
        entries = newEntries,
        isLoading = false,
        error = null
    )
}
```

The `setState` function is thread-safe via `MutableStateFlow.update()`.

### 3. Effect Handling

Effects are one-shot events like navigation or toast messages:

```kotlin
// Send an effect
Effect.NavigateToDetail(entryId).send()

// Or using setEffect directly
setEffect(Effect.ShowError("Something went wrong"))
```

Effects must be consumed by the UI calling `markEffectAsConsumed()`.

### 4. Event Processing

All user interactions flow through `processEvent()`:

```kotlin
override fun processEvent(event: Event) {
    when (event) {
        is Event.OnRefresh -> loadData()
        is Event.OnItemClicked -> handleItemClick(event.item)
        is Event.OnDeleteConfirmed -> deleteItem(event.itemId)
    }
}
```

## Usage Guide

### Step 1: Define the UI Contract

Create a contract object with three sealed interfaces:

```kotlin
object HomeUiContract {

    data class UiState(
        val entries: List<Entry> = emptyList(),
        val isLoading: Boolean = false,
        val selectedDate: LocalDate = LocalDate.now(),
        val errorMessage: String? = null
    ) : ViewState

    sealed interface Effect : ViewEffect {
        data class NavigateToDetail(val entryId: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface Event : ViewEvent {
        data class OnDateSelected(val date: LocalDate) : Event
        data class OnEntryClicked(val entry: Entry) : Event
        data class OnEntryToggled(val entry: Entry) : Event
        object OnRefreshRequested : Event
    }
}
```

### Step 2: Implement the ViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<UiState, Effect, Event>() {

    override fun getInitialState() = UiState()

    init {
        observeEntries()
    }

    override fun processEvent(event: Event) {
        when (event) {
            is Event.OnDateSelected -> onDateSelected(event.date)
            is Event.OnEntryClicked -> onEntryClicked(event.entry)
            is Event.OnEntryToggled -> onEntryToggled(event.entry)
            Event.OnRefreshRequested -> refresh()
        }
    }

    private fun observeEntries() {
        viewModelScope.launch(dispatcherProvider.io) {
            entryRepository.getEntriesVisibleOn(uiState.value.selectedDate)
                .collect { result ->
                    when (result) {
                        is ResultOperation.Success -> {
                            setState {
                                copy(entries = result.data, isLoading = false)
                            }
                        }
                        is ResultOperation.Error -> {
                            setState {
                                copy(isLoading = false, errorMessage = "Failed to load")
                            }
                        }
                    }
                }
        }
    }

    private fun onDateSelected(date: LocalDate) {
        setState { copy(selectedDate = date, isLoading = true) }
        // Re-observe with new date
        observeEntries()
    }

    private fun onEntryClicked(entry: Entry) {
        Effect.NavigateToDetail(entry.id).send()
    }

    private fun onEntryToggled(entry: Entry) {
        viewModelScope.launch(dispatcherProvider.io) {
            val updated = when (entry) {
                is Task -> entry.copy(isDone = !entry.isDone)
                is Habit -> entry.copy(isDone = !entry.isDone)
            }
            entryRepository.saveEntry(updated)
        }
    }

    private fun refresh() {
        setState { copy(isLoading = true) }
        viewModelScope.launch(dispatcherProvider.io) {
            entryRepository.syncEntries()
            setState { copy(isLoading = false) }
        }
    }
}
```

### Step 3: Connect to Compose UI

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToDetail -> {
                    onNavigateToDetail(effect.entryId)
                    viewModel.markEffectAsConsumed()
                }
                is Effect.ShowSnackbar -> {
                    // Show snackbar
                    viewModel.markEffectAsConsumed()
                }
                null -> { /* No pending effect */ }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::processEvent
    )
}

@Composable
private fun HomeContent(
    uiState: UiState,
    onEvent: (Event) -> Unit
) {
    // Render UI based on uiState
    // Call onEvent for user interactions
    LazyColumn {
        items(uiState.entries) { entry ->
            EntryItem(
                entry = entry,
                onClick = { onEvent(Event.OnEntryClicked(entry)) },
                onToggle = { onEvent(Event.OnEntryToggled(entry)) }
            )
        }
    }
}
```

## Best Practices

### Do

- Keep `UiState` immutable with data class
- Use sealed interfaces for exhaustive `when` expressions
- Consume effects immediately after handling
- Use `viewModelScope` for coroutine launches
- Inject `DispatcherProvider` for testability

### Don't

- Don't expose `MutableStateFlow` directly
- Don't perform side effects in `setState` reducer
- Don't forget to call `markEffectAsConsumed()`
- Don't access `uiState.value` in state reducer (use `this`)

## Testing

### Testing State Changes

```kotlin
@Test
fun `processEvent OnDateSelected updates selectedDate`() = runTest {
    val viewModel = HomeViewModel(FakeEntryRepository(), TestDispatcherProvider())

    viewModel.processEvent(Event.OnDateSelected(LocalDate.of(2024, 1, 15)))

    assertThat(viewModel.uiState.value.selectedDate)
        .isEqualTo(LocalDate.of(2024, 1, 15))
}
```

### Testing Effects with Turbine

```kotlin
@Test
fun `processEvent OnEntryClicked emits NavigateToDetail`() = runTest {
    val viewModel = HomeViewModel(FakeEntryRepository(), TestDispatcherProvider())
    val entry = Task.mock("123")

    viewModel.effect.test {
        viewModel.processEvent(Event.OnEntryClicked(entry))

        val effect = awaitItem()
        assertThat(effect).isInstanceOf(Effect.NavigateToDetail::class.java)
        assertThat((effect as Effect.NavigateToDetail).entryId).isEqualTo("123")
    }
}
```

### Test Dispatcher Provider

```kotlin
class TestDispatcherProvider(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}
```

## Related Documentation

- [ADR-002: MVI Pattern](../decisions/002-mvi-pattern.md)
- [Testing Guide](../guides/testing-guide.md)
