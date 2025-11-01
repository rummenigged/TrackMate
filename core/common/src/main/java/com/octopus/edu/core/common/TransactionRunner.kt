package com.octopus.edu.core.common

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
