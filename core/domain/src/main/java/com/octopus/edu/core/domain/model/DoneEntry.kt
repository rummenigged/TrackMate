package com.octopus.edu.core.domain.model

import java.time.Instant
import java.time.LocalDate

data class DoneEntry(
    val id: String,
    val date: LocalDate,
    val doneAt: Instant,
)
