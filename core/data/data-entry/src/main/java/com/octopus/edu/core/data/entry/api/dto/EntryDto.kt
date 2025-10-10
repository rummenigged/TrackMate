package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp

data class EntryDto(
    val id: String,
    val title: String,
    val description: String?,
    val isDone: Boolean = false,
    val time: Timestamp?,
    val type: String,
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val recurrence: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)
