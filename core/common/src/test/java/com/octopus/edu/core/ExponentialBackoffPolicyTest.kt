package com.octopus.edu.core

import com.octopus.edu.core.common.ExponentialBackoffPolicy
import com.octopus.edu.core.domain.model.common.ErrorType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExponentialBackoffPolicyTest {
    @Test
    fun `shouldRetry returns false for PermanentError`() =
        runTest {
            // Given
            val policy = ExponentialBackoffPolicy(initialDelay = 100, maxDelay = 1000)
            val error = ErrorType.PermanentError(Exception("Permanent failure"))

            // When
            val result = policy.shouldRetry(error, attempt = 0)

            // Then
            assertFalse(result)
            // Also assert that no delay occurred
            assertEquals(0, currentTime)
        }

    @Test
    fun `shouldRetry returns true for RetriableError`() =
        runTest {
            // Given
            val policy = ExponentialBackoffPolicy(initialDelay = 100, maxDelay = 1000)
            val error = ErrorType.TransientError(Exception("Temporary failure"))

            // When
            val result = policy.shouldRetry(error, attempt = 0)

            // Then
            assertTrue(result)
        }

    @Test
    fun `shouldRetry applies initial delay on first attempt`() =
        runTest {
            // Given
            val initialDelay = 1000L
            val policy = ExponentialBackoffPolicy(initialDelay = initialDelay, maxDelay = 5000)
            val error = ErrorType.TransientError(Exception("Temporary failure"))

            // When
            policy.shouldRetry(error, attempt = 0)

            // Then
            // Delay is initialDelay * (0 + 1) = 1000
            assertEquals(initialDelay, currentTime)
        }

    @Test
    fun `shouldRetry applies incremental delay for subsequent attempts`() =
        runTest {
            // Given
            val initialDelay = 1000L
            val policy = ExponentialBackoffPolicy(initialDelay = initialDelay, maxDelay = 10000)
            val error = ErrorType.TransientError(Exception("Temporary failure"))

            // When
            policy.shouldRetry(error, attempt = 2)

            // Then
            // Delay should be initialDelay * (2 + 1) = 3000
            assertEquals(3000, currentTime)
        }

    @Test
    fun `shouldRetry caps the delay at maxDelay`() =
        runTest {
            // Given
            val initialDelay = 1000L
            val maxDelay = 2500L
            val policy = ExponentialBackoffPolicy(initialDelay = initialDelay, maxDelay = maxDelay)
            val error = ErrorType.TransientError(Exception("Temporary failure"))

            // When
            // On attempt 3, the calculated delay would be initialDelay * (2 + 1) = 3000ms
            policy.shouldRetry(error, attempt = 2)

            // Then
            // The actual delay should be capped at maxDelay
            assertEquals(maxDelay, currentTime)
        }
}
