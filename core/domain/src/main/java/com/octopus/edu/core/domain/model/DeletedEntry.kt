package com.octopus.edu.core.domain.model

import java.time.Instant

data class DeletedEntry(
    val id: String,
    val deletedAt: Instant
)
