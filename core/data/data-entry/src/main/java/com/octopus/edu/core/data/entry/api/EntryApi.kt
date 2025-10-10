package com.octopus.edu.core.data.entry.api

import com.octopus.edu.core.domain.model.Entry

interface EntryApi {
    suspend fun saveEntry(entry: Entry)
}
