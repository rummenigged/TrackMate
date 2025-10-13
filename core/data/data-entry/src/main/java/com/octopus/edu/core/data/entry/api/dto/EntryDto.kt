package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp

data class EntryDto(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val isDone: Boolean = false,
    val time: Timestamp? = null,
    val type: String = "",
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val recurrence: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp? = null
)
