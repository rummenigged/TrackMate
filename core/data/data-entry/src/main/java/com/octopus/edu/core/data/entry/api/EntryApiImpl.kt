package com.octopus.edu.core.data.entry.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.network.utils.NetworkResponse
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class EntryApiImpl
    @Inject
    constructor(
        private val api: FirebaseFirestore,
    ) : EntryApi {
        companion object {
            const val COLLECTION_ENTRIES = "entries"
        }

        override suspend fun saveEntry(entry: Entry) {
            val entryDto = entry.toDTO()
            api
                .collection(COLLECTION_ENTRIES)
                .document(entryDto.id)
                .set(entryDto)
                .await()
        }

        override suspend fun fetchEntries(): NetworkResponse<List<EntryDto>> =
            try {
                val querySnapshot = api.collection(COLLECTION_ENTRIES).get().await()
                val entries = querySnapshot.toObjects<EntryDto>()
                NetworkResponse.Success(entries)
            } catch (e: Exception) {
                NetworkResponse.Error(e)
            }
    }
