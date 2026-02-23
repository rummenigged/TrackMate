# ADR-003: Offline-First Sync Strategy

## Status

Accepted

## Context

TrackMate needs to function fully without network connectivity while keeping data synchronized with Firebase Firestore when online. Requirements:

- Users can create, edit, and complete tasks/habits offline
- Changes sync automatically when connectivity returns
- Conflicts between local and server changes must be handled gracefully
- UI should reflect sync status for user confidence

## Decision

We adopted an **offline-first architecture** with Room as the single source of truth and background sync via WorkManager.

### Sync State Model

Each entry tracks its synchronization status:

```kotlin
enum class SyncState {
    PENDING,   // Local changes awaiting sync
    SYNCED,    // Synchronized with server
    FAILED,    // Sync attempted but failed
    CONFLICT   // Local and server versions conflict (not currently used)
}
```

In the database entity:

```kotlin
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    // ... other fields
    val syncState: SyncStateEntity,
    val updatedAt: Long
) {
    enum class SyncStateEntity {
        PENDING, SYNCED, FAILED, CONFLICT
    }
}
```

### Data Flow

#### Write Path (Local First)

```
┌─────────┐   save    ┌──────────────┐   observe   ┌─────────────┐
│   UI    │──────────►│     Room     │◄────────────│  SyncManager │
│         │           │ (PENDING)    │             │             │
└─────────┘           └──────────────┘             └──────┬──────┘
                                                          │ schedule
                                                   ┌──────▼──────┐
                                                   │ WorkManager  │
                                                   │   Worker     │
                                                   └──────┬──────┘
                                                          │ push
                                                   ┌──────▼──────┐
                                                   │  Firestore   │
                                                   └──────┬──────┘
                                                          │ success
                                                   ┌──────▼──────┐
                                                   │    Room      │
                                                   │  (SYNCED)    │
                                                   └──────────────┘
```

1. User creates/edits entry
2. Entry saved to Room with `syncState = PENDING`
3. `EntrySyncManager` observes `pendingEntries` Flow
4. Schedules `EntrySyncScheduler.scheduleEntrySync(entryId)`
5. WorkManager executes sync worker
6. On success, entry updated to `syncState = SYNCED`

#### Read Path (Local Only)

```
┌─────────┐   observe   ┌───────────┐   flow   ┌──────────┐
│   UI    │◄────────────│ ViewModel │◄─────────│   Room   │
└─────────┘             └───────────┘          └──────────┘
```

UI always reads from Room. No network calls on the read path.

### EntrySyncManager Implementation

```kotlin
class EntrySyncManager @Inject constructor(
    private val entryRepository: EntryRepository,
    private val syncScheduler: EntrySyncScheduler,
    private val errorClassifier: ErrorClassifier,
    private val retryPolicy: RetryPolicy,
    private val dispatcherProvider: DispatcherProvider,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start() {
        scope.launch(dispatcherProvider.io + exceptionHandler) {
            syncScheduler.scheduleBatchSync()

            launch { collectPendingEntries() }
            launch { collectDeletedEntries() }
        }
    }

    private suspend fun collectPendingEntries() {
        entryRepository.pendingEntries
            .distinctUntilChanged()
            .retryWhen { cause, attempt ->
                val errorType = errorClassifier.classify(cause)
                retryPolicy.shouldRetry(errorType, attempt)
            }
            .collect { entries ->
                entries.forEach { entry ->
                    syncScheduler.scheduleEntrySync(entry.id)
                }
            }
    }
}
```

### Repository Sync Operations

```kotlin
// EntryRepositoryImpl.kt

override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> {
    val mutex = entryLocks.getOrPut(entry.id) { Mutex() }

    return mutex.withLock {
        dbSemaphore.withPermit {
            safeCall(dispatcherProvider.io) {
                val entryDto = entry.toDto()
                when (val response = entryApi.pushEntry(entryDto)) {
                    is NetworkResponse.Success -> {
                        entryStore.updateSyncState(entry.id, SYNCED)
                    }
                    is NetworkResponse.Error -> {
                        val errorType = networkErrorClassifier.classify(response.error)
                        if (errorType is PermanentError) {
                            entryStore.updateSyncState(entry.id, FAILED)
                        }
                        throw response.error
                    }
                }
            }
        }
    }
}
```

### Batch Sync on App Start

On application start, `EntrySyncManager.start()` triggers:

1. `scheduleBatchSync()` - Syncs all pending entries
2. Starts collecting new pending entries for immediate sync

### Deleted Entry Handling

Deleted entries are tracked separately:

```kotlin
private suspend fun collectDeletedEntries() {
    entryRepository.deletedEntryIds
        .distinctUntilChanged()
        .collect { deletedEntries ->
            deletedEntries.forEach { entryId ->
                syncScheduler.scheduleDeletedEntrySync(entryId)
            }
        }
}
```

## Consequences

### Positive

- **Full offline support**: App works without network
- **Responsive UI**: Writes complete instantly (no network wait)
- **Data integrity**: Room transactions ensure consistency
- **Battery efficient**: WorkManager handles sync scheduling
- **Conflict awareness**: SyncState tracks potential issues

### Negative

- **Eventual consistency**: Server may lag behind local state
- **Complexity**: Sync logic adds significant complexity
- **Storage overhead**: Local database grows with unsynced changes
- **Conflict resolution**: Currently not implemented (CONFLICT state unused)

### Future Considerations

- Implement conflict resolution strategy
- Add periodic full sync for data integrity
- Consider server-side timestamp comparison

## Implementation Notes

### WorkManager Configuration

Sync workers use:
- `NetworkType.CONNECTED` constraint
- Exponential backoff for retries
- Unique work to prevent duplicates

### Concurrency Control

```kotlin
// Limit concurrent database operations
private val dbSemaphore: Semaphore // initialized with DB_SYNC_CONCURRENCY = 4

// Per-entry locking for consistency
private val entryLocks: ConcurrentHashMap<String, Mutex>
```

## Related Decisions

- [ADR-004: Error Classification & Retry Policy](004-error-classification-retry-policy.md)
- [Error Handling Pattern](../patterns/error-handling-pattern.md)

## References

- [Build offline-first apps](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
