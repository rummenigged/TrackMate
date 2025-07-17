package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EntryRepository {
    suspend fun getTasks(): ResultOperation<List<Task>>

    suspend fun getHabits(): ResultOperation<List<Habit>>

    suspend fun saveEntry(entry: Entry): ResultOperation<Unit>

    suspend fun getEntryById(id: String): ResultOperation<Entry>

    fun getEntriesVisibleOn(date: LocalDate = LocalDate.now()): Flow<ResultOperation<List<Entry>>>
}
