package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DeletedEntryDto(
    val id: String = "",
    @ServerTimestamp val deletedAt: Timestamp? = null
)
