# :core:domain

The domain module contains business entities, repository interfaces, and domain services. This module is pure Kotlin with no Android dependencies, enabling fast unit testing.

## Purpose

- Define core business models (Entry, Task, Habit)
- Declare repository contracts for data access
- Provide domain-level interfaces (ErrorClassifier, ReminderStrategy)

## Dependencies

### This module depends on:
- `:core:common` - Shared utilities

### Modules that depend on this:
- `:core:data:data-entry` - Implements `EntryRepository`
- `:core:data:data-auth` - Implements `AuthRepository`
- `:core:ui-common` - Uses domain models in ViewModels
- `:feature:*` - All feature modules use domain models

## Key Components

### Domain Models

| Model | Description | Location |
|-------|-------------|----------|
| `Entry` | Sealed class base for trackable items | `model/Entry.kt` |
| `Task` | Single-occurrence entry with due date | `model/Entry.kt` |
| `Habit` | Recurring entry with recurrence pattern | `model/Entry.kt` |
| `Reminder` | Reminder offset options | `model/Reminder.kt` |
| `Recurrence` | Habit recurrence patterns | `model/Recurrence.kt` |
| `SyncState` | Synchronization status enum | `model/Entry.kt` |

### Entry Hierarchy

```kotlin
sealed class Entry {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val isDone: Boolean
    abstract val time: LocalTime?
    abstract val createdAt: Instant
    abstract val updatedAt: Instant?
    abstract val reminder: Reminder?
    abstract val reminderType: ReminderType?
    abstract val syncState: SyncState
}

data class Task(...) : Entry() {
    val dueDate: LocalDate
}

data class Habit(...) : Entry() {
    val startDate: LocalDate
    val recurrence: Recurrence?
    val streakCount: Int?
    val lastCompletedDate: Instant?
}
```

### Repository Interfaces

| Interface | Purpose | Location |
|-----------|---------|----------|
| `EntryRepository` | Entry CRUD and sync operations | `repository/EntryRepository.kt` |
| `AuthRepository` | Authentication operations | `repository/AuthRepository.kt` |

```kotlin
interface EntryRepository {
    val pendingEntries: Flow<List<Entry>>
    val deletedEntryIds: Flow<List<String>>

    suspend fun saveEntry(entry: Entry): ResultOperation<Unit>
    suspend fun getEntryById(id: String): ResultOperation<Entry>
    suspend fun deleteEntry(entryId: String): ResultOperation<Unit>
    suspend fun syncEntries(): ResultOperation<Unit>
    fun getEntriesVisibleOn(date: LocalDate): Flow<ResultOperation<List<Entry>>>
}
```

### Domain Services

| Interface | Purpose | Location |
|-----------|---------|----------|
| `ErrorClassifier` | Classify errors as transient/permanent | `utils/ErrorClassifier.kt` |
| `ReminderStrategy` | Schedule/cancel entry reminders | `scheduler/ReminderStrategy.kt` |
| `EntrySyncScheduler` | Schedule background sync operations | `scheduler/EntrySyncScheduler.kt` |

### Common Types

| Type | Purpose | Location |
|------|---------|----------|
| `ResultOperation<T>` | Operation result wrapper | `model/common/ResultOperation.kt` |
| `ErrorType` | Error classification types | `model/common/ErrorType.kt` |
| `ReminderType` | Reminder delivery mechanism | `scheduler/ReminderType.kt` |

## Public API

### Entry Operations

```kotlin
// Creating entries
val task = Task(
    id = UUID.randomUUID().toString(),
    title = "Complete report",
    description = "Q4 financial report",
    isDone = false,
    time = LocalTime.of(14, 0),
    createdAt = Instant.now(),
    dueDate = LocalDate.now().plusDays(3),
    reminderType = ReminderType.NOTIFICATION,
    reminder = Reminder.OneHourEarly
)

val habit = Habit(
    id = UUID.randomUUID().toString(),
    title = "Morning meditation",
    description = "10 minutes mindfulness",
    isDone = false,
    time = LocalTime.of(7, 0),
    createdAt = Instant.now(),
    startDate = LocalDate.now(),
    recurrence = Recurrence.Daily
)
```

### Habit Applicability

```kotlin
// Check if habit applies to a specific date
fun Habit.appliesTo(date: LocalDate): Boolean {
    if (date.isBefore(startDate)) return false

    return when (recurrence) {
        Recurrence.Daily -> true
        Recurrence.Weekly -> startDate.dayOfWeek == date.dayOfWeek
        Recurrence.Custom, Recurrence.None, null -> startDate == date
    }
}
```

### Mock Factories (for testing)

```kotlin
// Quick mock creation for tests
val mockTask = Task.mock("1")
val mockHabit = Habit.mock("2")
```

## Testing

This module uses pure JUnit tests without Android dependencies:

```kotlin
class EntryTest {
    @Test
    fun `Habit appliesTo returns true for daily habit on any date after start`() {
        val habit = Habit(
            // ... properties
            startDate = LocalDate.of(2024, 1, 1),
            recurrence = Recurrence.Daily
        )

        assertThat(habit.appliesTo(LocalDate.of(2024, 1, 15))).isTrue()
        assertThat(habit.appliesTo(LocalDate.of(2024, 6, 30))).isTrue()
    }

    @Test
    fun `Habit appliesTo returns false for dates before startDate`() {
        val habit = Habit(
            // ... properties
            startDate = LocalDate.of(2024, 6, 1),
            recurrence = Recurrence.Daily
        )

        assertThat(habit.appliesTo(LocalDate.of(2024, 5, 31))).isFalse()
    }
}
```

Run tests:
```bash
./gradlew :core:domain:test
```

## Design Decisions

1. **Sealed classes for polymorphism**: `Entry`, `Reminder`, `Recurrence` use sealed classes for exhaustive when expressions
2. **Immutable data classes**: All models are immutable for thread safety
3. **No Android dependencies**: Enables fast unit tests and potential KMP support
4. **Interface-based contracts**: Repository interfaces enable dependency inversion

## Related Documentation

- [ADR-001: Modular Clean Architecture](../../docs/decisions/001-modular-clean-architecture.md)
- [Error Handling Pattern](../../docs/patterns/error-handling-pattern.md)
