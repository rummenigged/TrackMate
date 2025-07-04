package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.safeCall
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

class EntryRepositoryImpl
    @Inject
    constructor(
        private val entryStore: EntryStore,
    ) : EntryRepository {
        override suspend fun getTasks(): ResultOperation<List<Task>> =
            safeCall(
                dispatcher = Dispatchers.IO,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getTasks().mapNotNull { task -> task.toTaskOrNull() }
            }

        override suspend fun getHabits(): ResultOperation<List<Habit>> =
            safeCall(
                dispatcher = Dispatchers.IO,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getHabits().mapNotNull { task -> task.toHabitOrNull() }
            }
    }
