# ADR-004: Error Classification & Retry Policy

## Status

Accepted

## Context

TrackMate performs many asynchronous operations that can fail: database queries, network requests, and sync operations. We needed a strategy to:

- Distinguish retriable errors from permanent failures
- Apply consistent retry logic across the codebase
- Compose error handling for complex operations (e.g., sync = database + network)
- Enable proper error reporting to users and crash analytics

## Decision

We implemented an **error classification system** with composable classifiers and exponential backoff retry policy.

### Error Types

```kotlin
// core/domain/.../model/common/ErrorType.kt

sealed class ErrorType {
    data class TransientError(val cause: Throwable) : ErrorType()
    data class PermanentError(val cause: Throwable) : ErrorType()
}
```

- **TransientError**: Temporary failure, worth retrying (network timeout, disk full)
- **PermanentError**: Unrecoverable failure, don't retry (invalid data, auth failure)

### ErrorClassifier Interface

```kotlin
// core/domain/.../utils/ErrorClassifier.kt

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

### Specialized Classifiers

#### DatabaseErrorClassifier

```kotlin
class DatabaseErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        when (throwable) {
            is SQLiteDiskIOException,
            is SQLiteFullException,
            is SQLTimeoutException,
            is SQLiteCantOpenDatabaseException,
            is EntryNotFoundException -> true
            else -> false
        }
}
```

#### NetworkErrorClassifier

```kotlin
class NetworkErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        throwable is IOException ||
            throwable is FirebaseFirestoreException && throwable.isRetryable()
}

private fun FirebaseFirestoreException.isRetryable() =
    code in setOf(
        UNAVAILABLE,
        DEADLINE_EXCEEDED,
        RESOURCE_EXHAUSTED,
    )
```

#### SyncErrorClassifier (Composite)

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

### Hilt Module Configuration

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
        @DatabaseErrorClassifierQualifier databaseErrorClassifier: ErrorClassifier,
        @NetworkErrorClassifierQualifier networkErrorClassifier: ErrorClassifier
    ): ErrorClassifier =
        SyncErrorClassifier(databaseErrorClassifier, networkErrorClassifier)
}
```

### RetryPolicy

```kotlin
// core/common/.../RetryPolicy.kt

interface RetryPolicy {
    suspend fun shouldRetry(errorType: ErrorType, attempt: Long): Boolean
}

class ExponentialBackoffPolicy(
    private val maxAttempts: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 30000
) : RetryPolicy {

    override suspend fun shouldRetry(errorType: ErrorType, attempt: Long): Boolean {
        if (errorType is PermanentError) return false
        if (attempt >= maxAttempts) return false

        val delay = (baseDelayMs * 2.0.pow(attempt.toInt()))
            .toLong()
            .coerceAtMost(maxDelayMs)

        delay(delay)
        return true
    }
}
```

### Usage with Flow

```kotlin
// EntrySyncManager.kt

entryRepository.pendingEntries
    .distinctUntilChanged()
    .retryWhen { cause, attempt ->
        val errorType = errorClassifier.classify(cause)
        retryPolicy.shouldRetry(errorType, attempt)
    }
    .catch { e ->
        Logger.e("Error collecting pending entries", throwable = e)
    }
    .collect { entries ->
        // Process entries
    }
```

### Usage with suspend functions

```kotlin
// ResultOperation wrapper
sealed class ResultOperation<out T> {
    data class Success<T>(val data: T) : ResultOperation<T>()
    data class Error(
        val throwable: Throwable,
        val isRetriable: Boolean
    ) : ResultOperation<Nothing>()
}

// Safe call utility
suspend fun <T> safeCall(
    dispatcher: CoroutineDispatcher,
    onErrorReturn: ((Throwable) -> T)? = null,
    block: suspend () -> T
): ResultOperation<T> = withContext(dispatcher) {
    try {
        ResultOperation.Success(block())
    } catch (e: Exception) {
        if (onErrorReturn != null) {
            ResultOperation.Success(onErrorReturn(e))
        } else {
            ResultOperation.Error(e, isRetriable = false)
        }
    }
}
```

## Consequences

### Positive

- **Consistent retry behavior**: All operations use same classification
- **Composable**: `SyncErrorClassifier` combines database + network errors
- **Testable**: Each classifier is easily unit tested
- **Extensible**: New error types easy to add
- **Type-safe**: Sealed class ensures exhaustive handling

### Negative

- **Maintenance overhead**: New exception types must be added to classifiers
- **Potential over-retrying**: Aggressive retry can cause issues
- **Classification granularity**: Binary transient/permanent may be too simple

### Neutral

- **Learning curve**: Developers must understand classification system
- **Delay overhead**: Exponential backoff adds latency to recovery

## Testing

```kotlin
@Test
fun `NetworkErrorClassifier classifies IOException as transient`() {
    val classifier = NetworkErrorClassifier()
    val error = IOException("Connection reset")

    val result = classifier.classify(error)

    assertThat(result).isInstanceOf(TransientError::class.java)
}

@Test
fun `SyncErrorClassifier returns transient if either classifier returns transient`() {
    val syncClassifier = SyncErrorClassifier(
        DatabaseErrorClassifier(),
        NetworkErrorClassifier()
    )

    val ioError = IOException("Network error")
    assertThat(syncClassifier.classify(ioError)).isInstanceOf(TransientError::class.java)

    val sqlError = SQLiteDiskIOException()
    assertThat(syncClassifier.classify(sqlError)).isInstanceOf(TransientError::class.java)
}

@Test
fun `ExponentialBackoffPolicy stops after max attempts`() = runTest {
    val policy = ExponentialBackoffPolicy(maxAttempts = 3)
    val transientError = TransientError(IOException())

    assertThat(policy.shouldRetry(transientError, 0)).isTrue()
    assertThat(policy.shouldRetry(transientError, 1)).isTrue()
    assertThat(policy.shouldRetry(transientError, 2)).isTrue()
    assertThat(policy.shouldRetry(transientError, 3)).isFalse()
}

@Test
fun `ExponentialBackoffPolicy does not retry permanent errors`() = runTest {
    val policy = ExponentialBackoffPolicy()
    val permanentError = PermanentError(IllegalArgumentException())

    assertThat(policy.shouldRetry(permanentError, 0)).isFalse()
}
```

## Related Decisions

- [ADR-003: Offline-First Sync Strategy](003-offline-first-sync-strategy.md)
- [Error Handling Pattern](../patterns/error-handling-pattern.md)

## References

- [Exponential Backoff](https://en.wikipedia.org/wiki/Exponential_backoff)
- [Kotlin Flow retry operators](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/retry-when.html)
