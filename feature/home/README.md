# :feature:home

The home module implements the main screen of TrackMate, displaying tasks and habits for the selected date.

## Purpose

- Display user's tasks and habits for the current date
- Allow date navigation to view different days
- Enable task/habit completion toggling
- Navigate to entry details for editing

## Dependencies

### This module depends on:
- `:core:ui-common` - BaseViewModel, shared composables
- `:core:domain` - Entry, Task, Habit models
- `:core:design` - Design system

### No other modules depend on this module.

## Key Components

### HomeViewModel

Implements the MVI pattern via `BaseViewModel`:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<UiState, Effect, Event>()
```

**Responsibilities:**
- Load entries for selected date
- Handle date selection changes
- Process entry interactions
- Emit navigation effects

### HomeUiContract

Defines the screen's state, effects, and events:

```kotlin
object HomeUiContract {
    data class UiState(
        val entries: List<Entry> = emptyList(),
        val isLoading: Boolean = false,
        val selectedDate: LocalDate = LocalDate.now(),
        val error: String? = null
    ) : ViewState

    sealed interface Effect : ViewEffect {
        data class NavigateToDetail(val entryId: String) : Effect
        data class NavigateToCreate(val date: LocalDate) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed interface Event : ViewEvent {
        data class OnDateSelected(val date: LocalDate) : Event
        data class OnEntryClicked(val entry: Entry) : Event
        data class OnEntryToggled(val entry: Entry) : Event
        object OnAddClicked : Event
        object OnRefresh : Event
    }
}
```

### HomeScreen

Main composable for the home feature:

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: (LocalDate) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToDetail -> {
                    onNavigateToDetail(effect.entryId)
                    viewModel.markEffectAsConsumed()
                }
                is Effect.NavigateToCreate -> {
                    onNavigateToCreate(effect.date)
                    viewModel.markEffectAsConsumed()
                }
                is Effect.ShowError -> {
                    // Show snackbar
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

### UI Components

| Component | Purpose |
|-----------|---------|
| `HomeContent` | Main layout with date picker and entry list |
| `DateSelector` | Horizontal date picker for navigation |
| `EntryList` | LazyColumn of entries |
| `EntryItem` | Single entry card with completion toggle |
| `TaskItem` | Task-specific display with due date |
| `HabitItem` | Habit-specific display with streak info |

## Screen Structure

```
┌─────────────────────────────────────┐
│            Top App Bar              │
│         "Today's Tasks"             │
├─────────────────────────────────────┤
│         Date Selector               │
│  < Mon  Tue  [Wed]  Thu  Fri >     │
├─────────────────────────────────────┤
│                                     │
│         Entry List                  │
│  ┌─────────────────────────────┐   │
│  │ □ Complete report           │   │
│  │   Due: Today 2:00 PM        │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ ✓ Morning meditation        │   │
│  │   🔥 5 day streak           │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│              FAB (+)                │
└─────────────────────────────────────┘
```

## Data Flow

### Loading Entries

```
                      ┌───────────────┐
                      │   ViewModel   │
                      │  init block   │
                      └───────┬───────┘
                              │ observeEntries()
                      ┌───────▼───────┐
                      │  Repository   │
                      │ getEntries-   │
                      │ VisibleOn()   │
                      └───────┬───────┘
                              │ Flow<ResultOperation<List<Entry>>>
                      ┌───────▼───────┐
                      │    Room       │
                      │  (reactive)   │
                      └───────┬───────┘
                              │ emits on changes
                      ┌───────▼───────┐
                      │  ViewModel    │
                      │ setState()    │
                      └───────┬───────┘
                              │ StateFlow update
                      ┌───────▼───────┐
                      │   Compose     │
                      │ recomposes    │
                      └───────────────┘
```

### Entry Completion

```
User taps checkbox
        │
        ▼
onEvent(OnEntryToggled)
        │
        ▼
viewModel.processEvent()
        │
        ▼
repository.saveEntry(toggled)
        │
        ▼
Room updates, emits new list
        │
        ▼
UI recomposes with updated entry
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
    namespace = "com.octopus.edu.feature.home"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:ui-common"))
    implementation(project(":core:domain"))
    implementation(project(":core:design"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
```

## Testing

### ViewModel Tests

```kotlin
@Test
fun `initial state has today's date selected`() {
    val viewModel = createViewModel()

    assertThat(viewModel.uiState.value.selectedDate)
        .isEqualTo(LocalDate.now())
}

@Test
fun `OnDateSelected updates selectedDate and reloads entries`() = runTest {
    val viewModel = createViewModel()
    val newDate = LocalDate.of(2024, 6, 15)

    viewModel.processEvent(Event.OnDateSelected(newDate))
    advanceUntilIdle()

    assertThat(viewModel.uiState.value.selectedDate).isEqualTo(newDate)
    // Verify entries loaded for new date
}

@Test
fun `OnEntryClicked emits NavigateToDetail effect`() = runTest {
    val viewModel = createViewModel()
    val entry = Task.mock("123")

    viewModel.effect.test {
        viewModel.processEvent(Event.OnEntryClicked(entry))

        val effect = awaitItem()
        assertThat(effect).isInstanceOf(Effect.NavigateToDetail::class.java)
        assertThat((effect as Effect.NavigateToDetail).entryId).isEqualTo("123")
    }
}

@Test
fun `OnEntryToggled saves toggled entry`() = runTest {
    val repository = mockk<EntryRepository>(relaxed = true)
    val viewModel = createViewModel(repository)
    val task = Task.mock("1").copy(isDone = false)

    viewModel.processEvent(Event.OnEntryToggled(task))
    advanceUntilIdle()

    coVerify {
        repository.saveEntry(match { it.isDone == true })
    }
}
```

### UI Tests

```kotlin
@Test
fun `HomeContent displays entries`() {
    val entries = listOf(Task.mock("1"), Habit.mock("2"))
    val uiState = UiState(entries = entries)

    composeTestRule.setContent {
        HomeContent(uiState = uiState, onEvent = {})
    }

    composeTestRule.onNodeWithText("Task 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Habit 2").assertIsDisplayed()
}

@Test
fun `HomeContent shows loading indicator when loading`() {
    val uiState = UiState(isLoading = true)

    composeTestRule.setContent {
        HomeContent(uiState = uiState, onEvent = {})
    }

    composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        .assertIsDisplayed()
}

@Test
fun `EntryItem triggers OnEntryToggled on checkbox click`() {
    var toggledEntry: Entry? = null
    val task = Task.mock("1")

    composeTestRule.setContent {
        EntryItem(
            entry = task,
            onToggle = { toggledEntry = it },
            onClick = {}
        )
    }

    composeTestRule.onNodeWithTag("checkbox_1").performClick()

    assertThat(toggledEntry).isEqualTo(task)
}
```

Run tests:
```bash
./gradlew :feature:home:test
```

## Navigation

The home screen integrates with the app's navigation graph:

```kotlin
// In NavHost
composable(route = "home") {
    HomeScreen(
        onNavigateToDetail = { entryId ->
            navController.navigate("detail/$entryId")
        },
        onNavigateToCreate = { date ->
            navController.navigate("create?date=$date")
        }
    )
}
```

## Related Documentation

- [ADR-002: MVI Pattern](../../docs/decisions/002-mvi-pattern.md)
- [BaseViewModel Pattern](../../docs/patterns/base-viewmodel-pattern.md)
- [Adding a New Feature](../../docs/guides/adding-new-feature.md)
