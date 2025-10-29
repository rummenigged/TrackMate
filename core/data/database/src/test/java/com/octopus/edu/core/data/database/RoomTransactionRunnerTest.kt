package com.octopus.edu.core.data.database

import androidx.room.withTransaction
import com.octopus.edu.core.data.database.utils.RoomTransactionRunner
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class RoomTransactionRunnerTest {
    private val database: TrackMateDatabase = mockk()
    private val transactionRunner = RoomTransactionRunner(database)

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `run should execute block within transaction and return result`() =
        runTest {
            // Given
            val expectedResult = "Success"
            val block: suspend () -> String = { expectedResult }

            mockkStatic("androidx.room.RoomDatabaseKt")

            coEvery { database.withTransaction(any<suspend () -> String>()) } coAnswers {
                val lambda = it.invocation.args[1] as suspend () -> String
                lambda.invoke()
            }

            // When
            val result = transactionRunner.run(block)

            // Then
            assertEquals(expectedResult, result)
            coVerify(exactly = 1) { database.withTransaction(any<suspend () -> String>()) }
        }

    @Test
    fun `run should propagate exceptions from the block`() =
        runTest {
            // Given
            val exception = RuntimeException("Database operation failed")
            val block: suspend () -> Unit = { throw exception }

            mockkStatic("androidx.room.RoomDatabaseKt")

            coEvery { database.withTransaction(any<suspend () -> Unit>()) } coAnswers {
                val lambda = it.invocation.args[1] as suspend () -> Unit
                lambda.invoke()
            }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    transactionRunner.run(block)
                }

            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { database.withTransaction(any<suspend () -> Unit>()) }
        }
}
