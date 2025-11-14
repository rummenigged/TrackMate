package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class EntryDto(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val done: Boolean = false,
    val time: Timestamp? = null,
    val type: String = "",
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val recurrence: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
