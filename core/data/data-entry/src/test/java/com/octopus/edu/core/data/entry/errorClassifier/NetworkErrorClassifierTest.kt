package com.octopus.edu.core.data.entry.errorClassifier

import android.os.Build
import com.google.firebase.firestore.FirebaseFirestoreException
import com.octopus.edu.core.data.entry.NetworkErrorClassifier
import com.octopus.edu.core.domain.model.common.ErrorType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NetworkErrorClassifierTest {
    private val classifier = NetworkErrorClassifier()

    @Test
    fun `classify returns TransientError for IOException`() {
        // Given
        val error = IOException("Network connection lost")

        // When
        val result = classifier.classify(error)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for FirebaseFirestoreException with UNAVAILABLE code`() {
        // Given
        val firestoreException =
            FirebaseFirestoreException(
                "Service unavailable",
                FirebaseFirestoreException.Code.UNAVAILABLE,
            )

        // When
        val result = classifier.classify(firestoreException)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for FirebaseFirestoreException with DEADLINE_EXCEEDED code`() {
        // Given
        val firestoreException =
            FirebaseFirestoreException(
                "Deadline exceeded",
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            )

        // When
        val result = classifier.classify(firestoreException)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns TransientError for FirebaseFirestoreException with RESOURCE_EXHAUSTED code`() {
        // Given
        val firestoreException =
            FirebaseFirestoreException(
                "Resource exhausted",
                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
            )

        // When
        val result = classifier.classify(firestoreException)

        // Then
        assertIs<ErrorType.TransientError>(result)
    }

    @Test
    fun `classify returns PermanentError for FirebaseFirestoreException with non-retryable code`() {
        // Given
        val firestoreException =
            FirebaseFirestoreException(
                "Permission denied",
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
            )

        // When
        val result = classifier.classify(firestoreException)

        // Then
        assertIs<ErrorType.PermanentError>(result)
    }

    @Test
    fun `classify returns PermanentError for other exceptions`() {
        // Given
        val otherException = IllegalArgumentException("Invalid argument")

        // When
        val result = classifier.classify(otherException)

        // Then
        assertIs<ErrorType.PermanentError>(result)
    }
}
