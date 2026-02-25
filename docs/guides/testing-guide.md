# Testing Guide

This guide covers testing patterns and best practices in TrackMate.

## Testing Stack

| Library | Purpose | Usage |
|---------|---------|-------|
| JUnit 4 | Test framework | Unit tests |
| MockK | Kotlin mocking | Mock dependencies |
| Turbine | Flow testing | Test StateFlow/SharedFlow |
| Truth | Assertions | Readable assertions |
| Robolectric | Android unit tests | Test Android components without device |
| Compose Testing | UI tests | Test Compose UI |
| Hilt Testing | DI in tests | Integration tests |
| Konsist | Architecture tests | Enforce module boundaries |

## Test Structure

```
module/
└── src/
    ├── main/
    └── test/                    # Unit tests
        └── java/.../
            ├── ViewModelTest.kt
            ├── RepositoryTest.kt
            └── UseCaseTest.kt
    └── androidTest/             # Instrumented tests
        └── java/.../
            ├── DatabaseTest.kt
            └── ScreenTest.kt
```

## DispatcherProvider and TestDispatchers

### The Problem

ViewModels and repositories use coroutine dispatchers. In tests, we need control over execution timing.

### The Solution

Inject `DispatcherProvider` and swap with test implementation:

```kotlin
// Production
class DefaultDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
}

// Test
class TestDispatcherProvider(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : DispatcherProvider {
    override val main = testDispatcher
    override val io = testDispatcher
    override val default = testDispatcher
}
```

### MainDispatcherRule

For ViewModels that use `Dispatchers.Main`:

```kotlin
// core/testing/.../MainDispatcherRule.kt

class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

Usage:

```kotlin
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test something`() = runTest {
        // Test code
    }
}
```

## ViewModel Testing

### Basic Pattern

```kotlin
@HiltAndroidTest
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: EntryRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    private fun createViewModel() = HomeViewModel(
        entryRepository = repository,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `initial state has correct defaults`() {
        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.entries).isEmpty()
        assertThat(state.selectedDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `processEvent OnRefresh sets loading state`() = runTest {
        viewModel = createViewModel()

        viewModel.processEvent(Event.OnRefresh)

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }
}
```

### Testing State Changes

```kotlin
@Test
fun `loadEntries updates state with entries`() = runTest {
    val entries = listOf(Task.mock("1"), Task.mock("2"))
    coEvery { repository.getEntriesVisibleOn(any()) } returns flowOf(
        ResultOperation.Success(entries)
    )
    viewModel = createViewModel()

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertThat(state.entries).hasSize(2)
    assertThat(state.isLoading).isFalse()
}
```

### Testing Effects with Turbine

```kotlin
@Test
fun `OnEntryClicked emits NavigateToDetail effect`() = runTest {
    viewModel = createViewModel()
    val entry = Task.mock("123")

    viewModel.effect.test {
        viewModel.processEvent(Event.OnEntryClicked(entry))

        val effect = awaitItem()
        assertThat(effect).isInstanceOf(Effect.NavigateToDetail::class.java)
        assertThat((effect as Effect.NavigateToDetail).entryId).isEqualTo("123")
    }
}
```

### Testing Error Handling

```kotlin
@Test
fun `loadEntries sets error state on failure`() = runTest {
    coEvery { repository.getEntriesVisibleOn(any()) } returns flowOf(
        ResultOperation.Error(IOException("Network error"), isRetriable = true)
    )
    viewModel = createViewModel()

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertThat(state.error).isNotNull()
    assertThat(state.isLoading).isFalse()
}
```

## Repository Testing

### With Fake Data Sources

```kotlin
class EntryRepositoryImplTest {

    private lateinit var entryStore: FakeEntryStore
    private lateinit var entryApi: FakeEntryApi
    private lateinit var repository: EntryRepositoryImpl

    @Before
    fun setup() {
        entryStore = FakeEntryStore()
        entryApi = FakeEntryApi()
        repository = EntryRepositoryImpl(
            entryStore = entryStore,
            entryApi = entryApi,
            // ... other dependencies
        )
    }

    @Test
    fun `saveEntry stores entry with PENDING sync state`() = runTest {
        val task = Task.mock("1")

        repository.saveEntry(task)

        val saved = entryStore.getEntryById("1")
        assertThat(saved?.syncState).isEqualTo(SyncStateEntity.PENDING)
    }

    @Test
    fun `getEntriesVisibleOn filters habits correctly`() = runTest {
        val dailyHabit = Habit.mock("1").copy(
            recurrence = Recurrence.Daily,
            startDate = LocalDate.of(2024, 1, 1)
        )
        val task = Task.mock("2").copy(
            dueDate = LocalDate.of(2024, 1, 15)
        )
        entryStore.save(dailyHabit.toEntity())
        entryStore.save(task.toEntity())

        val result = repository.getEntriesVisibleOn(LocalDate.of(2024, 1, 15))
            .first { it is ResultOperation.Success }

        val entries = (result as ResultOperation.Success).data
        assertThat(entries).hasSize(2)
    }
}
```

### Testing Sync Operations

```kotlin
@Test
fun `pushEntry updates sync state to SYNCED on success`() = runTest {
    entryApi.pushResult = NetworkResponse.Success(Unit)
    val entry = Task.mock("1")
    entryStore.save(entry.toEntity())

    repository.pushEntry(entry)

    val updated = entryStore.getEntryById("1")
    assertThat(updated?.syncState).isEqualTo(SyncStateEntity.SYNCED)
}

@Test
fun `pushEntry updates sync state to FAILED on permanent error`() = runTest {
    entryApi.pushResult = NetworkResponse.Error(IllegalArgumentException())
    val entry = Task.mock("1")
    entryStore.save(entry.toEntity())

    repository.pushEntry(entry)

    val updated = entryStore.getEntryById("1")
    assertThat(updated?.syncState).isEqualTo(SyncStateEntity.FAILED)
}
```

## Error Classifier Testing

```kotlin
class DatabaseErrorClassifierTest {

    private val classifier = DatabaseErrorClassifier()

    @Test
    fun `classifies SQLiteFullException as transient`() {
        val error = SQLiteFullException("Disk full")

        val result = classifier.classify(error)

        assertThat(result).isInstanceOf(TransientError::class.java)
    }

    @Test
    fun `classifies IllegalArgumentException as permanent`() {
        val error = IllegalArgumentException("Invalid data")

        val result = classifier.classify(error)

        assertThat(result).isInstanceOf(PermanentError::class.java)
    }
}

class SyncErrorClassifierTest {

    private val classifier = SyncErrorClassifier(
        databaseErrorClassifier = DatabaseErrorClassifier(),
        networkErrorClassifier = NetworkErrorClassifier()
    )

    @Test
    fun `classifies IOException as transient`() {
        val error = IOException("Connection reset")

        assertThat(classifier.classify(error)).isInstanceOf(TransientError::class.java)
    }

    @Test
    fun `classifies SQLiteFullException as transient`() {
        val error = SQLiteFullException()

        assertThat(classifier.classify(error)).isInstanceOf(TransientError::class.java)
    }
}
```

## Retry Policy Testing

```kotlin
class ExponentialBackoffPolicyTest {

    @Test
    fun `shouldRetry returns true for transient errors within max attempts`() = runTest {
        val policy = ExponentialBackoffPolicy(maxAttempts = 3)
        val error = TransientError(IOException())

        assertThat(policy.shouldRetry(error, attempt = 0)).isTrue()
        assertThat(policy.shouldRetry(error, attempt = 1)).isTrue()
        assertThat(policy.shouldRetry(error, attempt = 2)).isTrue()
    }

    @Test
    fun `shouldRetry returns false after max attempts`() = runTest {
        val policy = ExponentialBackoffPolicy(maxAttempts = 3)
        val error = TransientError(IOException())

        assertThat(policy.shouldRetry(error, attempt = 3)).isFalse()
    }

    @Test
    fun `shouldRetry returns false for permanent errors`() = runTest {
        val policy = ExponentialBackoffPolicy()
        val error = PermanentError(IllegalArgumentException())

        assertThat(policy.shouldRetry(error, attempt = 0)).isFalse()
    }
}
```

## Compose UI Testing

### Basic UI Test

```kotlin
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays entries when loaded`() {
        val entries = listOf(Task.mock("1"), Task.mock("2"))
        val uiState = UiState(entries = entries)

        composeTestRule.setContent {
            HomeContent(uiState = uiState, onEvent = {})
        }

        composeTestRule.onNodeWithText("Task 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task 2").assertIsDisplayed()
    }

    @Test
    fun `shows loading indicator when loading`() {
        val uiState = UiState(isLoading = true)

        composeTestRule.setContent {
            HomeContent(uiState = uiState, onEvent = {})
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `calls onEvent when entry clicked`() {
        var clickedEntry: Entry? = null
        val entry = Task.mock("1")
        val uiState = UiState(entries = listOf(entry))

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onEvent = { event ->
                    if (event is Event.OnEntryClicked) {
                        clickedEntry = event.entry
                    }
                }
            )
        }

        composeTestRule.onNodeWithText("Task 1").performClick()

        assertThat(clickedEntry).isEqualTo(entry)
    }
}
```

### Testing with Hilt

```kotlin
@HiltAndroidTest
class HomeScreenIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `screen loads and displays entries`() {
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToDetail = {},
                onNavigateToCreate = {}
            )
        }

        // Wait for data to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("entry_item")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("entry_item")
            .assertCountEquals(/* expected count */)
    }
}
```

## Architecture Testing with Konsist

```kotlin
class ArchitectureTest {

    @Test
    fun `domain module has no Android dependencies`() {
        Konsist.scopeFromModule("core/domain")
            .files
            .assertFalse { file ->
                file.hasImport { import ->
                    import.name.startsWith("android.")
                }
            }
    }

    @Test
    fun `ViewModels extend BaseViewModel`() {
        Konsist.scopeFromProject()
            .classes()
            .withAnnotationOf(HiltViewModel::class)
            .assertTrue { clazz ->
                clazz.hasParentWithName("BaseViewModel")
            }
    }

    @Test
    fun `Repository implementations are internal`() {
        Konsist.scopeFromModule("core/data/data-entry")
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { clazz ->
                clazz.hasModifier(KoModifier.INTERNAL)
            }
    }
}
```

## Running Tests

### All Tests

```bash
./gradlew test
```

### Specific Module

```bash
./gradlew :core:domain:test
./gradlew :feature:home:test
```

### With Coverage

```bash
./gradlew testDebugUnitTest jacocoTestReport
```

Coverage report: `build/reports/jacoco/`

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Test Utilities

Located in `:core:testing`:

### MainDispatcherRule

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

### TestDispatcherProvider

```kotlin
val dispatcherProvider = TestDispatcherProvider()
```

### Mock Factories

```kotlin
val task = Task.mock("1")
val habit = Habit.mock("2")
```

### Fake Implementations

```kotlin
class FakeEntryStore : EntryStore {
    private val entries = mutableMapOf<String, EntryEntity>()

    override suspend fun save(entry: EntryEntity) {
        entries[entry.id] = entry
    }

    override suspend fun getEntryById(id: String) = entries[id]

    // ... other methods
}
```

## Best Practices

1. **Test behavior, not implementation** - Focus on what the code does, not how
2. **Use descriptive test names** - `methodName does something when condition`
3. **One assertion per test** - When practical
4. **Isolate dependencies** - Use mocks/fakes for external dependencies
5. **Test edge cases** - Empty lists, null values, error conditions
6. **Keep tests fast** - Use `UnconfinedTestDispatcher` for immediate execution

## Related Documentation

- [BaseViewModel Pattern](../patterns/base-viewmodel-pattern.md)
- [Error Handling Pattern](../patterns/error-handling-pattern.md)
