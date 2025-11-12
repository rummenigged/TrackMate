package com.octopus.edu.core.data.entry.api

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObjects
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.data.entry.UserPreferencesProvider
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.data.entry.utils.toDto
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.network.utils.NetworkResponse
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EntryApiImpl
    @Inject
    constructor(
        private val api: FirebaseFirestore,
        private val userPreferencesProvider: UserPreferencesProvider
    ) : EntryApi {
        override suspend fun saveEntry(entry: Entry): NetworkResponse<Unit> =
            try {
                val entryDto = entry.toDTO()
                withUserCollection { ref ->
                    ref
                        .collection(COLLECTION_ENTRIES)
                        .document(entryDto.id)
                        .set(entryDto)
                        .await()
                }
                NetworkResponse.Success(Unit)
            } catch (e: Exception) {
                Logger.e(
                    message = e.message ?: "Error saving entry",
                    throwable = e,
                )
                NetworkResponse.Error(e)
            }

        override suspend fun fetchEntries(): NetworkResponse<List<EntryDto>> =
            try {
                withUserCollection { ref ->
                    val querySnapshot =
                        ref
                            .collection(COLLECTION_ENTRIES)
                            .get()
                            .await()
                    val entries = querySnapshot.toObjects<EntryDto>()
                    NetworkResponse.Success(entries)
                }
            } catch (e: Exception) {
                Logger.e(
                    message = e.message ?: "Error fetching entry",
                    throwable = e,
                )
                NetworkResponse.Error(e)
            }

        override suspend fun pushDeletedEntry(entry: DeletedEntry): NetworkResponse<Unit> =
            try {
                val entryDto = entry.toDto()
                withUserCollection { ref ->
                    ref
                        .collection(COLLECTION_DELETED_ENTRIES)
                        .document(entryDto.id)
                        .set(entryDto)
                        .await()
                }
                NetworkResponse.Success(Unit)
            } catch (e: Exception) {
                Logger.e(
                    message = e.message ?: "Error pushing deleted entry",
                    throwable = e,
                )
                NetworkResponse.Error(e)
            }

        override suspend fun fetchDeletedEntry(): NetworkResponse<List<DeletedEntryDto>> =
            try {
                withUserCollection { ref ->
                    val querySnapshot = ref.collection(COLLECTION_DELETED_ENTRIES).get().await()
                    val deletedEntries = querySnapshot.toObjects<DeletedEntryDto>()
                    NetworkResponse.Success(deletedEntries)
                }
            } catch (e: Exception) {
                Logger.e(
                    message = e.message ?: "Error fetching deleted entry",
                    throwable = e,
                )
                NetworkResponse.Error(e)
            }

        override suspend fun pushDoneEntry(entry: DoneEntry): NetworkResponse<Unit> =
            try {
                val entryDto = entry.toDto()
                withUserCollection { ref ->
                    ref
                        .collection(COLLECTION_DONE_ENTRIES)
                        .document(entry.id)
                        .set(entryDto)
                        .await()
                }
                NetworkResponse.Success(Unit)
            } catch (e: Exception) {
                Logger.e(
                    message = e.message ?: "Error pushing done entry",
                    throwable = e,
                )
                NetworkResponse.Error(e)
            }

        private suspend inline fun <T> withUserCollection(block: suspend (DocumentReference) -> T): T {
            val userId =
                userPreferencesProvider.userId
                    ?.takeIf { it.isNotBlank() }
                    ?: throw FirebaseFirestoreException(
                        "User not authenticated",
                        FirebaseFirestoreException.Code.UNAUTHENTICATED,
                    )

            val userDoc = api.collection(COLLECTION_USERS).document(userId)
            return block(userDoc)
        }

        companion object {
            const val COLLECTION_USERS = "users"
            const val COLLECTION_ENTRIES = "entries"
            const val COLLECTION_DELETED_ENTRIES = "deleted_entries"
            const val COLLECTION_DONE_ENTRIES = "done_entries"
        }
    }
