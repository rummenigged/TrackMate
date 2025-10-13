package com.octopus.edu.core.data.entry.api

import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.network.utils.NetworkResponse

interface EntryApi {
    suspend fun saveEntry(entry: Entry)

    suspend fun fetchEntries(): NetworkResponse<List<EntryDto>>
}
