# ADR-002: MVI Pattern

## Status

Accepted

## Context

We needed a presentation layer architecture that:

- Provides predictable state management for Compose UI
- Handles one-shot events (navigation, toasts) cleanly
- Reduces boilerplate across ViewModels
- Enables straightforward testing with Flows
- Works well with Kotlin Coroutines and Flow

Traditional approaches with multiple `StateFlow` properties lead to state synchronization issues. We needed a pattern with a single source of truth for UI state.

## Decision

We adopted the **MVI (Model-View-Intent) pattern** implemented through a generic `BaseViewModel`:

```kotlin
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

### MVI Components

The pattern maps directly to MVI concepts:

| MVI Concept | Implementation | Purpose                               |
|-------------|----------------|---------------------------------------|
| **Model** | `UiState` | Single immutable state object         |
| **View** | Compose UI | Renders state, emits events           |
| **Intent** | `Event` | Actions that trigger state changes    |
| **Effect** | `Effect` | One-shot side effects (MVI extension) |

### Three Contract Types

Each screen defines three sealed interfaces:

1. **ViewState** - Immutable UI state (what to display)
2. **ViewEffect** - One-shot side effects (navigation, toasts)
3. **ViewEvent** - App Intents (clicks, input changes)

### Example: Home Screen Contract

```kotlin
// feature/home/.../HomeUiContract.kt

object HomeUiContract {
    data class UiState(
        val entries: ImmutableList<Entry> = persistentListOf(),
        val isLoading: Boolean = false,
        val selectedDate: LocalDate = LocalDate.now(),
        val error: String? = null
    ) : ViewState

    sealed interface Effect : ViewEffect {
        data class NavigateToDetail(val entryId: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed interface Event : ViewEvent {
        data class OnDateSelected(val date: LocalDate) : Event
        data class OnEntryClicked(val entry: Entry) : Event
        data class OnEntryCompleted(val entry: Entry) : Event
        object OnRefresh : Event
    }
}
```

### Example: HomeViewModel Implementation

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : BaseViewModel<UiState, Effect, Event>() {

    override fun getInitialState() = UiState()

    init {
        loadEntries()
    }

    override fun processEvent(event: Event) {
        when (event) {
            is Event.OnDateSelected -> onDateSelected(event.date)
            is Event.OnEntryClicked -> navigateToDetail(event.entry)
            is Event.OnEntryCompleted -> markEntryComplete(event.entry)
            Event.OnRefresh -> loadEntries()
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            entryRepository.getEntriesVisibleOn(uiState.value.selectedDate)
                .collect { result ->
                    when (result) {
                        is ResultOperation.Success -> {
                            setState {
                                copy(entries = result.data, isLoading = false, error = null)
                            }
                        }
                        is ResultOperation.Error -> {
                            setState { copy(isLoading = false, error = "Failed to load entries") }
                        }
                    }
                }
        }
    }

    private fun navigateToDetail(entry: Entry) {
        Effect.NavigateToDetail(entry.id).send()
    }
}
```

### Compose UI Integration

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
                is Effect.ShowError -> {
                    // Show snackbar
                    viewModel.markEffectAsConsumed()
                }
                null -> { /* No effect pending */ }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::processEvent
    )
}
```

### Unidirectional Data Flow

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│    ┌─────────┐   Event    ┌────────────┐   State       │
│    │  View   │───────────►│  ViewModel │───────────┐   │
│    │(Compose)│            │            │           │   │
│    └─────────┘            └────────────┘           │   │
│         ▲                                          │   │
│         │                                          │   │
│         └──────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Consequences

### Positive

- **Single source of truth**: One `UiState` object represents entire screen state
- **Predictable updates**: State changes through `setState` reducer function
- **Testable**: State and effects can be tested via Flow emissions
- **Type-safe events**: Sealed interface ensures exhaustive handling
- **Reduced boilerplate**: `BaseViewModel` handles common patterns
- **Unidirectional flow**: Easy to reason about state changes

### Negative

- **Learning curve**: Developers must understand state/effect/event pattern
- **Verbosity**: Three sealed interfaces per screen
- **Effect consumption**: Manual `markEffectAsConsumed()` call required

### Neutral

- **State copying**: Large state objects may have copy overhead (negligible in practice)

## Testing

The pattern enables straightforward testing:

```kotlin
@Test
fun `processEvent OnRefresh loads entries`() = runTest {
    val viewModel = HomeViewModel(fakeRepository, testDispatcherProvider)

    viewModel.processEvent(Event.OnRefresh)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertThat(state.entries).isNotEmpty()
    assertThat(state.isLoading).isFalse()
}

@Test
fun `processEvent OnEntryClicked emits NavigateToDetail effect`() = runTest {
    val viewModel = HomeViewModel(fakeRepository, testDispatcherProvider)
    val testEntry = Task.mock("1")

    viewModel.effect.test {
        viewModel.processEvent(Event.OnEntryClicked(testEntry))

        val effect = awaitItem()
        assertThat(effect).isInstanceOf(Effect.NavigateToDetail::class.java)
        assertThat((effect as Effect.NavigateToDetail).entryId).isEqualTo("1")
    }
}
```

## Related Decisions

- [ADR-001: Modular Clean Architecture](001-modular-clean-architecture.md)
- [BaseViewModel Pattern](../patterns/base-viewmodel-pattern.md)

## References

- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [State holders and UI State](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [MVI Architecture](https://hannesdorfmann.com/android/model-view-intent/)
