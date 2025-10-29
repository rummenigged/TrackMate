package com.octopus.edu.core.data.entry.errorClassifier

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import com.octopus.edu.core.data.entry.DatabaseErrorClassifier
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.domain.model.common.ErrorType
import org.junit.Test
import java.sql.SQLTimeoutException
import kotlin.test.assertIs

class DatabaseErrorClassifierTest {
    private val classifier = DatabaseErrorClassifier()

    @Test
    fun `classify returns TransientError for SQLiteDiskIOException`() {
        // Given
        val error = SQLiteDiskIOException("Disk IO error")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for SQLiteFullException`() {
        // Given
        val error = SQLiteFullException("Database full")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for SQLTimeoutException`() {
        // Given
        val error = SQLTimeoutException("Timeout")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for EntryNotFoundException`() {
        // Given
        val error = EntryNotFoundException("Element not found")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for SQLiteCantOpenDatabaseException`() {
        // Given
        val error = SQLiteCantOpenDatabaseException("Can't open DB")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns PermanentError for other exceptions`() {
        // Given
        val error = IllegalArgumentException("Some other error")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.PermanentError>(result)
    }
}
