package com.octopus.edu.core.data.entry.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.octopus.edu.core.data.entry.UserPreferencesProvider
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.data.entry.utils.toDto
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.network.utils.NetworkResponse
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class EntryApiImpl
    @Inject
    constructor(
        private val api: FirebaseFirestore,
        private val userPreferencesProvider: UserPreferencesProvider
    ) : EntryApi {
        private val userId
            get() = userPreferencesProvider.userId
        private val userCollection
            get() = api.collection(COLLECTION_USERS).document(userId)

        companion object {
            const val COLLECTION_USERS = "users"
            const val COLLECTION_ENTRIES = "entries"
            const val COLLECTION_DELETED_ENTRIES = "deleted_entries"
        }

        /**
         * Persists the given Entry for the current user in the user's entries collection in Firestore.
         *
         * @param entry Entry to persist; its id is used as the Firestore document ID.
         */
        override suspend fun saveEntry(entry: Entry) {
            val entryDto = entry.toDTO()
            userCollection
                .collection(COLLECTION_ENTRIES)
                .document(entryDto.id)
                .set(entryDto)
                .await()
        }

        /**
             * Fetches all entry DTOs for the current user from Firestore.
             *
             * @return `NetworkResponse.Success` containing the list of `EntryDto` on success,
             *         or `NetworkResponse.Error` containing the thrown exception on failure.
             */
            override suspend fun fetchEntries(): NetworkResponse<List<EntryDto>> =
            try {
                val querySnapshot =
                    userCollection
                        .collection(COLLECTION_ENTRIES)
                        .get()
                        .await()
                val entries = querySnapshot.toObjects<EntryDto>()
                NetworkResponse.Success(entries)
            } catch (e: Exception) {
                NetworkResponse.Error(e)
            }

        /**
         * Saves a deleted entry for the current user to Firestore.
         *
         * The provided DeletedEntry is converted to its DTO representation and stored in the
         * user's "deleted_entries" subcollection using the DTO's `id` as the document ID.
         *
         * @param entry The DeletedEntry to persist for the current user.
         */
        override suspend fun pushDeletedEntry(entry: DeletedEntry) {
            val entryDto = entry.toDto()
            userCollection
                .collection(COLLECTION_DELETED_ENTRIES)
                .document(entryDto.id)
                .set(entryDto)
                .await()
        }

        /**
             * Fetches all deleted entry DTOs for the current user from Firestore.
             *
             * @return `NetworkResponse.Success` containing the list of `DeletedEntryDto` on success, `NetworkResponse.Error` with the caught exception on failure.
             */
            override suspend fun fetchDeletedEntry(): NetworkResponse<List<DeletedEntryDto>> =
            try {
                val querySnapshot = userCollection.collection(COLLECTION_DELETED_ENTRIES).get().await()
                val deletedEntries = querySnapshot.toObjects<DeletedEntryDto>()
                NetworkResponse.Success(deletedEntries)
            } catch (e: Exception) {
                NetworkResponse.Error(e)
            }
    }