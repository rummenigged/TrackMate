# :core:ui-common

The ui-common module provides shared UI infrastructure including `BaseViewModel`, common composables, and UI utilities.

## Purpose

- Provide `BaseViewModel` base class for consistent state management
- Share reusable Compose components
- Define UI-level utilities and extensions

## Dependencies

### This module depends on:
- `:core:domain` - Domain models for ViewModels
- `:core:design` - Design system tokens

### Modules that depend on this:
- `:feature:home`
- `:feature:history`
- `:feature:analytics`
- `:feature:signIn`

## Key Components

### BaseViewModel

The foundation for all ViewModels in the application:

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

**Key Features:**
- Single `UiState` via `StateFlow` for predictable UI
- Nullable `Effect` flow for one-shot events
- Type-safe `Event` processing
- Thread-safe state updates

### UI Contract Pattern

Each screen defines its contract:

```kotlin
object ScreenUiContract {
    data class UiState(
        // Screen state
    ) : ViewState

    sealed interface Effect : ViewEffect {
        // One-shot effects (navigation, toasts)
    }

    sealed interface Event : ViewEvent {
        // User interactions
    }
}
```

### Common Composables

#### LoadingIndicator

```kotlin
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = MaterialTheme.colorScheme.primary
    )
}
```

#### ErrorState

```kotlin
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
```

#### EmptyState

```kotlin
@Composable
fun EmptyState(
    title: String,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### UI Extensions

#### Modifier Extensions

```kotlin
// Conditional modifier
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) modifier() else this

// Click with ripple
fun Modifier.clickableWithRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = rememberRipple(),
    enabled = enabled,
    onClick = onClick
)
```

#### State Extensions

```kotlin
// Collect as state with lifecycle awareness
@Composable
fun <T> StateFlow<T>.collectAsStateLifecycle(): State<T> =
    collectAsStateWithLifecycle()
```

## Usage Pattern

### 1. Define Contract

```kotlin
// feature/home/HomeUiContract.kt
object HomeUiContract {
    data class UiState(
        val entries: List<Entry> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    sealed interface Effect : ViewEffect {
        data class NavigateToDetail(val id: String) : Effect
    }

    sealed interface Event : ViewEvent {
        data class OnEntryClicked(val entry: Entry) : Event
        object OnRefresh : Event
    }
}
```

### 2. Implement ViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: EntryRepository
) : BaseViewModel<UiState, Effect, Event>() {

    override fun getInitialState() = UiState()

    override fun processEvent(event: Event) {
        when (event) {
            is Event.OnEntryClicked -> {
                Effect.NavigateToDetail(event.entry.id).send()
            }
            Event.OnRefresh -> refresh()
        }
    }

    private fun refresh() {
        setState { copy(isLoading = true) }
        // Load data...
    }
}
```

### 3. Connect UI

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToDetail -> {
                    onNavigate(effect.id)
                    viewModel.markEffectAsConsumed()
                }
                null -> {}
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::processEvent
    )
}
```

## Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.core.ui.common"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:design"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
}
```

## Testing

### ViewModel Tests

```kotlin
@Test
fun `setState updates uiState`() = runTest {
    val viewModel = TestViewModel()

    viewModel.testSetState { copy(isLoading = true) }

    assertThat(viewModel.uiState.value.isLoading).isTrue()
}

@Test
fun `setEffect emits effect`() = runTest {
    val viewModel = TestViewModel()

    viewModel.effect.test {
        viewModel.testSetEffect(TestEffect.ShowMessage("Hello"))

        val effect = awaitItem()
        assertThat(effect).isEqualTo(TestEffect.ShowMessage("Hello"))
    }
}

@Test
fun `markEffectAsConsumed clears effect`() = runTest {
    val viewModel = TestViewModel()
    viewModel.testSetEffect(TestEffect.ShowMessage("Hello"))

    viewModel.markEffectAsConsumed()

    assertThat(viewModel.effect.value).isNull()
}
```

### Composable Tests

```kotlin
@Test
fun `LoadingIndicator displays circular progress`() {
    composeTestRule.setContent {
        LoadingIndicator()
    }

    composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        .assertIsDisplayed()
}

@Test
fun `ErrorState displays message and retry button`() {
    var retryClicked = false

    composeTestRule.setContent {
        ErrorState(
            message = "Something went wrong",
            onRetry = { retryClicked = true }
        )
    }

    composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    composeTestRule.onNodeWithText("Retry").performClick()
    assertThat(retryClicked).isTrue()
}
```

Run tests:
```bash
./gradlew :core:ui-common:test
```

## Design Principles

1. **Separation of concerns**: ViewModels handle logic, Composables handle rendering
2. **Unidirectional data flow**: State down, events up
3. **Immutable state**: All UI state is immutable
4. **Testable**: ViewModels tested with Turbine, Composables with Compose Testing

## Related Documentation

- [ADR-002: MVI Pattern](../../docs/decisions/002-mvi-pattern.md)
- [BaseViewModel Pattern](../../docs/patterns/base-viewmodel-pattern.md)
- [Testing Guide](../../docs/guides/testing-guide.md)
