# Architecture Overview

This document describes the architectural decisions and patterns used in TrackMate.

## Design Philosophy

TrackMate is built on three core principles:

1. **Testability** - Every component can be tested in isolation
2. **Offline-First** - The app functions fully without network connectivity
3. **Modularity** - Clear boundaries enable parallel development and reuse

## Layered Architecture

The application follows Clean Architecture with three distinct layers:

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│   ViewModels, Compose UI, Navigation, UI State          │
│   (:feature:*, :core:ui-common, :core:design)           │
└────────────────────────┬────────────────────────────────┘
                         │ depends on
┌────────────────────────▼────────────────────────────────┐
│                      DOMAIN LAYER                        │
│   Entities, Repository Interfaces, Use Cases             │
│   (:core:domain)                                         │
└────────────────────────┬────────────────────────────────┘
                         │ implemented by
┌────────────────────────▼────────────────────────────────┐
│                       DATA LAYER                         │
│   Repository Implementations, Data Sources, Mappers      │
│   (:core:data:*, :core:network, :core:data:database)    │
└─────────────────────────────────────────────────────────┘
```

### Presentation Layer

The presentation layer uses the MVI (Model-View-Intent) pattern through `BaseViewModel`:

- **State**: Single immutable `UiState` exposed via `StateFlow`
- **Events**: User interactions dispatched through `processEvent()`
- **Effects**: One-shot side effects (navigation, toasts) via `Effect` flow

See [MVI Pattern](docs/decisions/002-mvi-pattern.md) for details.

### Domain Layer

The domain layer contains:

- **Entities**: `Entry`, `Task`, `Habit`, `Reminder`, `Recurrence`
- **Repository Interfaces**: `EntryRepository`, `AuthRepository`
- **Domain Services**: `ErrorClassifier`, `ReminderStrategy`

The domain module has no Android dependencies, enabling pure unit testing.

### Data Layer

The data layer implements repository interfaces and manages:

- **Local Storage**: Room database with `EntryEntity`
- **Remote Sync**: Firebase Firestore via `EntryApi`
- **Sync State**: `PENDING`, `SYNCED`, `FAILED`, `CONFLICT`

## Module Structure

### Dependency Rules

```
┌─────────────────────────────────────────────────────────┐
│  RULE: Dependencies flow downward only                  │
│                                                          │
│  :feature:* ──► :core:ui-common ──► :core:domain        │
│                                          │               │
│  :core:data:* ─────────────────────────►─┘               │
│       │                                                  │
│       └──► :core:database, :core:network, :core:common  │
└─────────────────────────────────────────────────────────┘
```

### Module Responsibilities

| Module | Responsibility | Dependencies |
|--------|----------------|--------------|
| `:app` | App lifecycle, DI root, navigation | All modules |
| `:core:domain` | Business entities and contracts | `:core:common` |
| `:core:data:data-entry` | Entry persistence and sync | `:core:domain`, `:core:database`, `:core:network` |
| `:core:data:database` | Room database and DAOs | None (data-only) |
| `:core:network` | Firebase/Retrofit clients | None (infra-only) |
| `:core:ui-common` | BaseViewModel, shared composables | `:core:domain`, `:core:design` |
| `:core:common` | Utilities, dispatchers, logging | None |
| `:feature:*` | Screen-specific UI and ViewModels | `:core:ui-common`, `:core:domain` |

## Cross-Cutting Concerns

### Dependency Injection

Hilt manages all dependencies with the following scope hierarchy:

```
@Singleton (Application-scoped)
    └── @ViewModelScoped (ViewModel-scoped)
            └── @ActivityScoped (Activity-scoped)
```

Key Hilt modules:

- `ErrorClassifierModule` - Error classification strategies
- `ReminderStrategyModule` - Reminder scheduling strategies
- `DispatcherModule` - Coroutine dispatchers
- `DatabaseModule` - Room database and DAOs

### Coroutine Dispatchers

All dispatchers are injected via `DispatcherProvider` interface:

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
```

This enables test dispatchers to be swapped in during testing.

### Logging

Centralized logging through `Logger` object:

```kotlin
Logger.d("Tag", "Debug message")
Logger.e("Tag", "Error message", throwable)
```

In debug builds, logs go to Logcat. In release, critical errors go to Crashlytics.

### Error Handling

Errors are classified by type to determine retry behavior:

```kotlin
sealed class ErrorType {
    data class TransientError(val cause: Throwable) : ErrorType()
    data class PermanentError(val cause: Throwable) : ErrorType()
}
```

Error classifiers form a composable chain:

```
SyncErrorClassifier
    ├── DatabaseErrorClassifier
    └── NetworkErrorClassifier
```

See [Error Classification & Retry Policy](docs/decisions/004-error-classification-retry-policy.md).

## Data Flow

### Read Flow (Offline-First)

```
┌────────────┐    observe    ┌────────────┐    flow    ┌──────────┐
│  Compose   │◄──────────────│ ViewModel  │◄───────────│   Room   │
│    UI      │               │            │            │ Database │
└────────────┘               └────────────┘            └──────────┘
```

1. UI observes ViewModel's `StateFlow<UiState>`
2. ViewModel collects from repository's `Flow<List<Entry>>`
3. Repository streams directly from Room (source of truth)

### Write Flow (With Sync)

```
┌────────────┐   event    ┌────────────┐   save    ┌──────────┐
│  Compose   │───────────►│ ViewModel  │──────────►│   Room   │
│    UI      │            │            │           │(PENDING) │
└────────────┘            └────────────┘           └────┬─────┘
                                                        │ observe
                          ┌────────────┐   push    ┌────▼─────┐
                          │  Firebase  │◄──────────│   Sync   │
                          │ Firestore  │           │ Manager  │
                          └────────────┘           └──────────┘
```

1. User action triggers event in ViewModel
2. ViewModel saves entry to Room with `syncState = PENDING`
3. `EntrySyncManager` observes pending entries
4. WorkManager schedules background sync to Firebase
5. On success, entry marked `SYNCED`

## Testing Strategy

### Unit Tests

- Domain models: Pure Kotlin tests
- ViewModels: Test with `TestDispatcher` and `Turbine`
- Repositories: Test with in-memory Room database

### Integration Tests

- Database: Room instrumented tests
- API: Firebase emulator tests

### Architecture Tests

Konsist enforces architectural rules:

```kotlin
@Test
fun `domain module has no Android dependencies`() {
    Konsist.scopeFromModule("core/domain")
        .files
        .assertFalse { it.hasImport { import -> import.startsWith("android.") } }
}
```

## Performance

### Baseline Profiles

The `:baselineprofile` module generates startup profiles for AOT compilation of critical paths.

### Database Concurrency

Sync operations use a semaphore to limit concurrent database access:

```kotlin
buildConfigField("int", "DB_SYNC_CONCURRENCY", "4")
```

### Flow Optimization

Repository flows use `distinctUntilChanged()` to prevent redundant emissions:

```kotlin
entryStore.streamPendingEntries()
    .distinctUntilChanged()
    .flowOn(dispatcherProvider.io)
```

## Further Reading

- [ADR-001: Modular Clean Architecture](docs/decisions/001-modular-clean-architecture.md)
- [ADR-002: MVI Pattern](docs/decisions/002-mvi-pattern.md)
- [ADR-003: Offline-First Sync Strategy](docs/decisions/003-offline-first-sync-strategy.md)
- [BaseViewModel Pattern](docs/patterns/base-viewmodel-pattern.md)
- [Error Handling Pattern](docs/patterns/error-handling-pattern.md)
