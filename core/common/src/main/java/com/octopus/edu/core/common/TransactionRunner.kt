package com.octopus.edu.core.common

interface TransactionRunner {
    /**
 * Executes the provided suspending block within the runner's context and returns its result.
 *
 * @param block The suspending lambda to execute.
 * @return The result produced by [block].
 */
suspend fun <T> run(block: suspend () -> T): T
}