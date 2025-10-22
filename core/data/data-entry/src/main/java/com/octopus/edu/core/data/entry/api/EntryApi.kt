package com.octopus.edu.core.data.entry.api

import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.network.utils.NetworkResponse

interface EntryApi {
    /**
 * Saves the given entry via the API.
 *
 * @param entry The domain model Entry to save.
 */
suspend fun saveEntry(entry: Entry)

    /**
 * Fetches the current entries from the remote API.
 *
 * @return A NetworkResponse wrapping the list of entries as `EntryDto` objects.
 */
suspend fun fetchEntries(): NetworkResponse<List<EntryDto>>

    /**
 * Pushes a deleted entry to the remote API to synchronize deletion state.
 *
 * @param entry The deleted entry to be pushed to the server.
 */
suspend fun pushDeletedEntry(entry: DeletedEntry)

    /**
 * Retrieves entries that have been marked deleted from the remote service.
 *
 * @return A NetworkResponse containing a list of DeletedEntryDto representing deleted entries.
 */
suspend fun fetchDeletedEntry(): NetworkResponse<List<DeletedEntryDto>>
}