# :core:common

The common module provides shared utilities, extension functions, and cross-cutting infrastructure used throughout the application.

## Purpose

- Provide coroutine dispatcher abstraction for testability
- Centralize logging infrastructure
- Define retry policies for error recovery
- Offer common extension functions

## Dependencies

### This module depends on:
- None (leaf module)

### Modules that depend on this:
- `:core:domain`
- `:core:data:*`
- `:core:ui-common`
- `:app`

## Key Components

### DispatcherProvider

Abstracts coroutine dispatchers for testability:

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

**Usage:**
```kotlin
class MyRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun loadData() = withContext(dispatcherProvider.io) {
        // IO operation
    }
}
```

**Testing:**
```kotlin
class TestDispatcherProvider(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : DispatcherProvider {
    override val main = testDispatcher
    override val io = testDispatcher
    override val default = testDispatcher
}
```

### Logger

Centralized logging with environment-aware behavior:

```kotlin
object Logger {
    fun d(tag: String = "TrackMate", message: String) {
        // Debug log (Logcat in debug, no-op in release)
    }

    fun e(tag: String = "TrackMate", message: String, throwable: Throwable? = null) {
        // Error log (Logcat + Crashlytics in release)
    }

    fun i(tag: String = "TrackMate", message: String) {
        // Info log
    }

    fun w(tag: String = "TrackMate", message: String) {
        // Warning log
    }
}
```

**Usage:**
```kotlin
Logger.d("MyClass", "Processing started")
Logger.e("MyClass", "Operation failed", exception)
```

### RetryPolicy

Defines retry behavior for failed operations:

```kotlin
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

**Delay Schedule:**
| Attempt | Delay |
|---------|-------|
| 0 | 1000ms |
| 1 | 2000ms |
| 2 | 4000ms |
| 3+ | Stop |

### Extension Functions

#### Date/Time Extensions

```kotlin
// Convert LocalDate to epoch milliseconds
fun LocalDate.toEpochMilli(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// Convert Instant to LocalDate
fun Instant.toLocalDate(): LocalDate =
    atZone(ZoneId.systemDefault()).toLocalDate()

// Format for display
fun LocalDate.formatDisplay(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
```

#### Flow Extensions

```kotlin
// Debounce state changes
fun <T> Flow<T>.debounceState(timeoutMillis: Long): Flow<T> =
    debounce(timeoutMillis).distinctUntilChanged()
```

#### String Extensions

```kotlin
// Capitalize first letter
fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

// Truncate with ellipsis
fun String.truncate(maxLength: Int): String =
    if (length > maxLength) take(maxLength - 3) + "..." else this
```

## Hilt Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider =
        DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideRetryPolicy(): RetryPolicy =
        ExponentialBackoffPolicy()
}
```

## Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.core.common"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
```

## Testing

### DispatcherProvider Tests

```kotlin
@Test
fun `DefaultDispatcherProvider provides correct dispatchers`() {
    val provider = DefaultDispatcherProvider()

    assertThat(provider.main).isEqualTo(Dispatchers.Main)
    assertThat(provider.io).isEqualTo(Dispatchers.IO)
    assertThat(provider.default).isEqualTo(Dispatchers.Default)
}
```

### RetryPolicy Tests

```kotlin
@Test
fun `ExponentialBackoffPolicy retries transient errors up to maxAttempts`() = runTest {
    val policy = ExponentialBackoffPolicy(maxAttempts = 3)
    val transient = TransientError(IOException())

    assertThat(policy.shouldRetry(transient, 0)).isTrue()
    assertThat(policy.shouldRetry(transient, 1)).isTrue()
    assertThat(policy.shouldRetry(transient, 2)).isTrue()
    assertThat(policy.shouldRetry(transient, 3)).isFalse()
}

@Test
fun `ExponentialBackoffPolicy never retries permanent errors`() = runTest {
    val policy = ExponentialBackoffPolicy()
    val permanent = PermanentError(IllegalArgumentException())

    assertThat(policy.shouldRetry(permanent, 0)).isFalse()
}
```

### Extension Function Tests

```kotlin
@Test
fun `toEpochMilli converts LocalDate correctly`() {
    val date = LocalDate.of(2024, 1, 15)

    val epochMilli = date.toEpochMilli()

    // Verify by converting back
    val restored = Instant.ofEpochMilli(epochMilli)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    assertThat(restored).isEqualTo(date)
}

@Test
fun `truncate shortens long strings`() {
    val longString = "This is a very long string that needs truncation"

    val truncated = longString.truncate(20)

    assertThat(truncated).isEqualTo("This is a very lo...")
    assertThat(truncated.length).isEqualTo(20)
}
```

Run tests:
```bash
./gradlew :core:common:test
```

## Design Principles

1. **No Android dependencies**: Pure Kotlin for maximum portability
2. **Interface-based**: `DispatcherProvider`, `RetryPolicy` enable test doubles
3. **Single responsibility**: Each utility serves one clear purpose
4. **Minimal dependencies**: Leaf module with no project dependencies

## Related Documentation

- [ADR-004: Error Classification & Retry Policy](../../docs/decisions/004-error-classification-retry-policy.md)
- [Testing Guide](../../docs/guides/testing-guide.md)
