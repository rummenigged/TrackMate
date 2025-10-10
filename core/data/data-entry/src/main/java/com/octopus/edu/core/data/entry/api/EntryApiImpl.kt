package com.octopus.edu.core.data.entry.api

import com.google.firebase.firestore.FirebaseFirestore
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.domain.model.Entry
import kotlinx.coroutines.tasks.await
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
    }
