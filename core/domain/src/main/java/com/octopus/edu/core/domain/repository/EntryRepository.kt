package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation

interface EntryRepository {
    suspend fun getTasks(): ResultOperation<List<Task>>

    suspend fun getHabits(): ResultOperation<List<Habit>>

    suspend fun getEntries(): ResultOperation<List<Entry>>
}
