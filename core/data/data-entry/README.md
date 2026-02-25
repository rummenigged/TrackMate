# :core:data:data-entry

The data-entry module implements the `EntryRepository` interface, providing entry persistence, synchronization, and error handling.

## Purpose

- Implement `EntryRepository` from `:core:domain`
- Manage local database operations via Room
- Handle synchronization with Firebase Firestore
- Provide error classification for database and network errors

## Dependencies

### This module depends on:
- `:core:domain` - Repository interface, domain models
- `:core:common` - DispatcherProvider, utilities
- `:core:network` - API clients
- `:core:data:database` - Room database, DAOs

### Modules that depend on this:
- `:app` - Uses `EntryRepository` for sync orchestration

## Key Components

### EntryRepositoryImpl

Primary implementation of `EntryRepository`:

```kotlin
internal class EntryRepositoryImpl @Inject constructor(
    private val entryStore: EntryStore,
    private val entryApi: EntryApi,
    private val reminderStore: ReminderStore,
    private val dbSemaphore: Semaphore,
    private val entryLocks: ConcurrentHashMap<String, Mutex>,
    @DatabaseErrorClassifierQualifier
    private val databaseErrorClassifier: ErrorClassifier,
    @NetworkErrorClassifierQualifier
    private val networkErrorClassifier: ErrorClassifier,
    private val dispatcherProvider: DispatcherProvider
) : EntryRepository
```

**Key Features:**
- Offline-first with Room as source of truth
- Sync state tracking (PENDING, SYNCED, FAILED)
- Concurrent access control via Semaphore and Mutex
- Error classification for retry decisions

### Error Classifiers

| Classifier | Purpose | Transient Errors |
|------------|---------|------------------|
| `DatabaseErrorClassifier` | Database errors | SQLiteFullException, SQLTimeoutException |
| `NetworkErrorClassifier` | Network errors | IOException, Firestore UNAVAILABLE |
| `SyncErrorClassifier` | Combined sync errors | Delegates to DB + Network classifiers |

```kotlin
class SyncErrorClassifier @Inject constructor(
    @DatabaseErrorClassifierQualifier
    private val databaseErrorClassifier: ErrorClassifier,
    @NetworkErrorClassifierQualifier
    private val networkErrorClassifier: ErrorClassifier
) : BaseErrorClassifier() {

    override fun isTransient(throwable: Throwable): Boolean =
        databaseErrorClassifier.classify(throwable) is TransientError ||
            networkErrorClassifier.classify(throwable) is TransientError
}
```

### Data Mappers

| Function | From | To |
|----------|------|-----|
| `EntryEntity.toDomain()` | Entity | Entry |
| `Entry.toEntity()` | Entry | Entity |
| `EntryEntity.toTaskOrNull()` | Entity | Task? |
| `EntryEntity.toHabitOrNull()` | Entity | Habit? |
| `Entry.toDto()` | Entry | EntryDto |

### Hilt Modules

#### ErrorClassifierModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ErrorClassifierModule {
    @DatabaseErrorClassifierQualifier
    @Provides
    fun provideDatabaseErrorClassifier(): ErrorClassifier =
        DatabaseErrorClassifier()

    @NetworkErrorClassifierQualifier
    @Provides
    fun provideNetworkErrorClassifier(): ErrorClassifier =
        NetworkErrorClassifier()

    @SyncErrorClassifierQualifier
    @Provides
    fun provideSyncErrorClassifier(
        @DatabaseErrorClassifierQualifier db: ErrorClassifier,
        @NetworkErrorClassifierQualifier network: ErrorClassifier
    ): ErrorClassifier = SyncErrorClassifier(db, network)
}
```

#### EntryRepositoryModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class EntryRepositoryModule {
    @Binds
    abstract fun bindEntryRepository(impl: EntryRepositoryImpl): EntryRepository
}
```

## Public API

### Reactive Streams

```kotlin
// Observe entries for a specific date
repository.getEntriesVisibleOn(LocalDate.now())
    .collect { result ->
        when (result) {
            is ResultOperation.Success -> handleEntries(result.data)
            is ResultOperation.Error -> handleError(result.throwable)
        }
    }

// Observe pending entries (for sync)
repository.pendingEntries.collect { entries -> /* schedule sync */ }

// Observe deleted entry IDs (for sync)
repository.deletedEntryIds.collect { ids -> /* schedule delete sync */ }
```

### CRUD Operations

```kotlin
// Save entry (marks as PENDING for sync)
repository.saveEntry(task)

// Get entry by ID
when (val result = repository.getEntryById("entry-123")) {
    is ResultOperation.Success -> showEntry(result.data)
    is ResultOperation.Error -> showError()
}

// Delete entry (schedules delete sync)
repository.deleteEntry("entry-123")
```

### Sync Operations

```kotlin
// Push single entry to server
repository.pushEntry(entry)

// Sync all pending entries
repository.syncEntries()
```

## Concurrency Control

### Database Semaphore

Limits concurrent database operations to prevent resource exhaustion:

```kotlin
// Configured in build.gradle.kts
buildConfigField("int", "DB_SYNC_CONCURRENCY", "4")

// Usage
dbSemaphore.withPermit {
    // Database operation
}
```

### Per-Entry Mutex

Prevents concurrent modifications to the same entry:

```kotlin
private val entryLocks: ConcurrentHashMap<String, Mutex>

val mutex = entryLocks.getOrPut(entry.id) { Mutex() }
mutex.withLock {
    // Entry-specific operation
}
```

## Build Configuration

```kotlin
// build.gradle.kts
android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("int", "DB_SYNC_CONCURRENCY", "4")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:data:database"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
}
```

## Testing

### Repository Tests

```kotlin
@Test
fun `saveEntry marks entry as PENDING`() = runTest {
    val repository = createRepository()
    val task = Task.mock("1")

    repository.saveEntry(task)

    val saved = repository.getEntryById("1")
    assertThat((saved as ResultOperation.Success).data.syncState)
        .isEqualTo(SyncState.PENDING)
}

@Test
fun `getEntriesVisibleOn filters habits by date`() = runTest {
    val repository = createRepository()
    val dailyHabit = Habit.mock("1").copy(recurrence = Recurrence.Daily)
    val weeklyHabit = Habit.mock("2").copy(
        recurrence = Recurrence.Weekly,
        startDate = LocalDate.of(2024, 1, 1) // Monday
    )

    repository.saveEntry(dailyHabit)
    repository.saveEntry(weeklyHabit)

    // Tuesday - daily applies, weekly doesn't
    repository.getEntriesVisibleOn(LocalDate.of(2024, 1, 2))
        .first { it is ResultOperation.Success }
        .let { result ->
            val entries = (result as ResultOperation.Success).data
            assertThat(entries).hasSize(1)
            assertThat(entries[0].id).isEqualTo("1")
        }
}
```

### Error Classifier Tests

```kotlin
@Test
fun `SyncErrorClassifier returns transient for IOException`() {
    val classifier = SyncErrorClassifier(
        DatabaseErrorClassifier(),
        NetworkErrorClassifier()
    )

    val result = classifier.classify(IOException("Network error"))

    assertThat(result).isInstanceOf(TransientError::class.java)
}
```

Run tests:
```bash
./gradlew :core:data:data-entry:test
```

## Related Documentation

- [ADR-003: Offline-First Sync Strategy](../../docs/decisions/003-offline-first-sync-strategy.md)
- [ADR-004: Error Classification & Retry Policy](../../docs/decisions/004-error-classification-retry-policy.md)
- [Error Handling Pattern](../../docs/patterns/error-handling-pattern.md)
