package com.octopus.edu.core.data.entry.api.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DoneEntryDto(
    val id: String = "",
    @ServerTimestamp val date: Timestamp? = null,
    @ServerTimestamp val doneAt: Timestamp? = null
)
