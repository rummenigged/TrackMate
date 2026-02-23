# Error Handling Pattern

This document describes the error handling patterns used throughout TrackMate.

## Overview

TrackMate uses a multi-layered error handling approach:

1. **ResultOperation** - Wrapper for operation outcomes
2. **ErrorClassifier** - Categorizes errors as transient or permanent
3. **RetryPolicy** - Determines retry behavior based on error type
4. **Flow operators** - Apply retry logic to reactive streams

## ResultOperation

`ResultOperation<T>` is a sealed class that wraps operation results:

```kotlin
sealed class ResultOperation<out T> {
    data class Success<T>(val data: T) : ResultOperation<T>()
    data class Error(
        val throwable: Throwable,
        val isRetriable: Boolean
    ) : ResultOperation<Nothing>()
}
```

### Usage in Repository

```kotlin
override suspend fun getEntryById(id: String): ResultOperation<Entry> =
    safeCall(dispatcherProvider.io) {
        entryStore.getEntryById(id)?.toDomain()
            ?: throw EntryNotFoundException(id)
    }
```

### Usage in ViewModel

```kotlin
private fun loadEntry(entryId: String) {
    viewModelScope.launch {
        when (val result = entryRepository.getEntryById(entryId)) {
            is ResultOperation.Success -> {
                setState { copy(entry = result.data, isLoading = false) }
            }
            is ResultOperation.Error -> {
                setState { copy(error = "Entry not found", isLoading = false) }
                if (result.isRetriable) {
                    // Optionally schedule retry
                }
            }
        }
    }
}
```

## safeCall Utility

The `safeCall` function wraps suspending operations in try-catch:

```kotlin
suspend fun <T : Any> safeCall(
    dispatcher: CoroutineDispatcher,
    onErrorReturn: (() -> T)? = null,
    isRetriableWhen: ((Throwable) -> Boolean)? = null,
    doOnError: (suspend (Throwable) -> Unit)? = null,
    block: suspend () -> T,
): ResultOperation<T> =
    withContext(dispatcher) {
        try {
            ResultOperation.Success(block())
        } catch (e: Throwable) {
            if (onErrorReturn != null) {
                ResultOperation.Success(onErrorReturn())
            } else {
                try {
                    doOnError?.invoke(e)
                } catch (errorHandlerException: Throwable) {
                    e.addSuppressed(errorHandlerException)
                }
                if (isRetriableWhen?.invoke(e) == true) {
                    ResultOperation.Error(e, true)
                } else {
                    ResultOperation.Error(e)
                }
            }
        }
    }
```

### With Fallback Value

```kotlin
override suspend fun getTasks(): ResultOperation<List<Task>> =
    safeCall(
        dispatcher = dispatcherProvider.io,
        onErrorReturn = { emptyList() }  // Return empty list on error
    ) {
        entryStore.getTasks().mapNotNull { it.toTaskOrNull() }
    }
```

## Error Classification

### ErrorType

```kotlin
sealed class ErrorType {
    data class TransientError(val cause: Throwable) : ErrorType()
    data class PermanentError(val cause: Throwable) : ErrorType()
}
```

### ErrorClassifier Interface

```kotlin
interface ErrorClassifier {
    fun classify(throwable: Throwable): ErrorType
}

abstract class BaseErrorClassifier : ErrorClassifier {
    override fun classify(throwable: Throwable): ErrorType {
        return if (isTransient(throwable)) {
            ErrorType.TransientError(throwable)
        } else {
            ErrorType.PermanentError(throwable)
        }
    }

    abstract fun isTransient(throwable: Throwable): Boolean
}
```

### Classifier Implementations

#### DatabaseErrorClassifier

Handles SQLite and Room errors:

```kotlin
class DatabaseErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        when (throwable) {
            is SQLiteDiskIOException -> true      // Disk I/O error
            is SQLiteFullException -> true         // Disk full
            is SQLTimeoutException -> true         // Query timeout
            is SQLiteCantOpenDatabaseException -> true
            is EntryNotFoundException -> true      // Entry deleted elsewhere
            else -> false
        }
}
```

#### NetworkErrorClassifier

Handles network and Firebase errors:

```kotlin
class NetworkErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        throwable is IOException ||
            throwable is FirebaseFirestoreException && throwable.isRetryable()
}

private fun FirebaseFirestoreException.isRetryable() =
    code in setOf(
        UNAVAILABLE,        // Service unavailable
        DEADLINE_EXCEEDED,  // Timeout
        RESOURCE_EXHAUSTED  // Rate limited
    )
```

#### SyncErrorClassifier (Composite)

Combines database and network classifiers:

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

## Retry Policy

### Interface

```kotlin
interface RetryPolicy {
    suspend fun shouldRetry(errorType: ErrorType, attempt: Long): Boolean
}
```

### ExponentialBackoffPolicy

```kotlin
class ExponentialBackoffPolicy(
    private val maxAttempts: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 30000
) : RetryPolicy {

    override suspend fun shouldRetry(errorType: ErrorType, attempt: Long): Boolean {
        // Never retry permanent errors
        if (errorType is PermanentError) return false

        // Stop after max attempts
        if (attempt >= maxAttempts) return false

        // Calculate delay with exponential backoff
        val delay = (baseDelayMs * 2.0.pow(attempt.toInt()))
            .toLong()
            .coerceAtMost(maxDelayMs)

        delay(delay)
        return true
    }
}
```

### Delay Schedule

| Attempt | Delay |
|---------|-------|
| 0 | 1s |
| 1 | 2s |
| 2 | 4s |
| 3+ | Stop retrying |

## Flow Retry Operators

### Using retryWhen

```kotlin
entryRepository.pendingEntries
    .distinctUntilChanged()
    .retryWhen { cause, attempt ->
        val errorType = errorClassifier.classify(cause)
        retryPolicy.shouldRetry(errorType, attempt)
    }
    .catch { e ->
        Logger.e("Failed after retries", throwable = e)
    }
    .collect { entries ->
        // Process entries
    }
```

### Custom Retry Extension

```kotlin
fun <T> Flow<ResultOperation<T>>.retryOnResultError(
    errorClassifier: ErrorClassifier,
    retryPolicy: RetryPolicy
): Flow<ResultOperation<T>> = retryWhen { cause, attempt ->
    val errorType = errorClassifier.classify(cause)
    retryPolicy.shouldRetry(errorType, attempt)
}
```

## Repository Pattern

### Read Operations with Error Handling

```kotlin
override fun getEntriesVisibleOn(date: LocalDate): Flow<ResultOperation<List<Entry>>> =
    entryStore
        .getEntriesBeforeOrOn(date.toEpochMilli())
        .map { entries ->
            ResultOperation.Success(
                entries.mapNotNull { it.toDomain() }
                    .filter { it is Task || (it as Habit).appliesTo(date) }
            ) as ResultOperation<List<Entry>>
        }
        .catch { exception ->
            emit(
                ResultOperation.Error(
                    throwable = exception,
                    isRetriable = databaseErrorClassifier.classify(exception) is TransientError
                )
            )
        }
        .flowOn(dispatcherProvider.io)
```

### Write Operations with Error Handling

```kotlin
override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> {
    val mutex = entryLocks.getOrPut(entry.id) { Mutex() }

    return mutex.withLock {
        dbSemaphore.withPermit {
            safeCall(dispatcherProvider.io) {
                when (val response = entryApi.pushEntry(entry.toDto())) {
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

## Error Handling Layers

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│  - Display user-friendly error messages                 │
│  - Offer retry action for retriable errors              │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  ViewModel                               │
│  - Map ResultOperation.Error to UI state                │
│  - Decide whether to show error or retry silently       │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Repository                              │
│  - Wrap operations in safeCall/ResultOperation          │
│  - Apply error classification for isRetriable flag      │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                Sync Layer                                │
│  - Use retryWhen with classifier + policy               │
│  - Log errors after max retries                         │
└─────────────────────────────────────────────────────────┘
```

## Testing

### Testing Error Classification

```kotlin
@Test
fun `DatabaseErrorClassifier classifies SQLiteFullException as transient`() {
    val classifier = DatabaseErrorClassifier()

    val result = classifier.classify(SQLiteFullException("Disk full"))

    assertThat(result).isInstanceOf(TransientError::class.java)
}

@Test
fun `DatabaseErrorClassifier classifies IllegalArgumentException as permanent`() {
    val classifier = DatabaseErrorClassifier()

    val result = classifier.classify(IllegalArgumentException("Invalid input"))

    assertThat(result).isInstanceOf(PermanentError::class.java)
}
```

### Testing Retry Policy

```kotlin
@Test
fun `ExponentialBackoffPolicy retries transient errors`() = runTest {
    val policy = ExponentialBackoffPolicy(maxAttempts = 3)

    assertThat(policy.shouldRetry(TransientError(IOException()), 0)).isTrue()
    assertThat(policy.shouldRetry(TransientError(IOException()), 1)).isTrue()
    assertThat(policy.shouldRetry(TransientError(IOException()), 2)).isTrue()
    assertThat(policy.shouldRetry(TransientError(IOException()), 3)).isFalse()
}

@Test
fun `ExponentialBackoffPolicy never retries permanent errors`() = runTest {
    val policy = ExponentialBackoffPolicy()

    assertThat(policy.shouldRetry(PermanentError(IllegalArgumentException()), 0)).isFalse()
}
```

### Testing Repository with Errors

```kotlin
@Test
fun `getEntryById returns Error when entry not found`() = runTest {
    val store = FakeEntryStore(entries = emptyList())
    val repository = EntryRepositoryImpl(store, ...)

    val result = repository.getEntryById("nonexistent")

    assertThat(result).isInstanceOf(ResultOperation.Error::class.java)
    assertThat((result as ResultOperation.Error).throwable)
        .isInstanceOf(EntryNotFoundException::class.java)
}
```

## Related Documentation

- [ADR-004: Error Classification & Retry Policy](../decisions/004-error-classification-retry-policy.md)
- [ADR-003: Offline-First Sync Strategy](../decisions/003-offline-first-sync-strategy.md)
