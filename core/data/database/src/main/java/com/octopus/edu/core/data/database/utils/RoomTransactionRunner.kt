package com.octopus.edu.core.data.database.utils

import androidx.room.withTransaction
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.TrackMateDatabase

class RoomTransactionRunner(
    private val database: TrackMateDatabase
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = database.withTransaction { block() }
}
