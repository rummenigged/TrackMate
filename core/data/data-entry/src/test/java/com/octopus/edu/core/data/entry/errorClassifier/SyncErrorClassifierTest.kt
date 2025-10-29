package com.octopus.edu.core.data.entry.errorClassifier

import com.octopus.edu.core.data.entry.DatabaseErrorClassifier
import com.octopus.edu.core.data.entry.NetworkErrorClassifier
import com.octopus.edu.core.data.entry.SyncErrorClassifier
import com.octopus.edu.core.domain.model.common.ErrorType
import org.junit.Test
import java.sql.SQLTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SyncErrorClassifierTest {
    private val databaseErrorClassifier = DatabaseErrorClassifier()
    private val networkErrorClassifier = NetworkErrorClassifier()
    private val errorClassifier =
        SyncErrorClassifier(
            databaseErrorClassifier,
            networkErrorClassifier,
        )

    @Test
    fun `classify returns TransientError for SQLTimeoutException`() {
        // Given
        val timeoutException = SQLTimeoutException("Database timeout")

        // When
        val errorType = errorClassifier.classify(timeoutException)

        // Then
        assertIs<ErrorType.TransientError>(errorType)
        assertEquals(timeoutException, errorType.cause)
    }

    @Test
    fun `classify returns PermanentError for other exceptions`() {
        // Given
        val otherException = IllegalArgumentException("Invalid argument")

        // When
        val errorType = errorClassifier.classify(otherException)

        // Then
        assertIs<ErrorType.PermanentError>(errorType)
        assertEquals(otherException, errorType.cause)
    }

    @Test
    fun `classify returns PermanentError for a generic Exception`() {
        // Given
        val genericException = Exception("Something went wrong")

        // When
        val errorType = errorClassifier.classify(genericException)

        // Then
        assertIs<ErrorType.PermanentError>(errorType)
        assertEquals(genericException, errorType.cause)
    }
}
