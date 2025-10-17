package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp

data class DeletedEntryDto(
    val id: String = "",
    val deletedAt: Timestamp = Timestamp.now()
)
