package com.octopus.edu.core.data.database.utils

import androidx.room.withTransaction
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.TrackMateDatabase

class RoomTransactionRunner(
    private val database: TrackMateDatabase
) : TransactionRunner {
    /**
 * Executes the given suspend block inside a Room database transaction.
 *
 * If the block throws an exception the transaction is rolled back and the exception propagates.
 *
 * @param block The suspendable lambda to run within the transaction.
 * @return The value returned by `block`.
 */
override suspend fun <T> run(block: suspend () -> T): T = database.withTransaction { block() }
}