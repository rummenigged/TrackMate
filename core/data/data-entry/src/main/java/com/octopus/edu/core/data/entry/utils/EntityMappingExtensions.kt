package com.octopus.edu.core.data.entry.utils

import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task

internal fun EntryEntity.toDomain(): Entry? =
    when (type) {
        EntryType.TASK -> toTaskOrNull()
        EntryType.HABIT -> toHabitOrNull()
    }

internal fun EntryEntity.toTaskOrNull(): Task? {
    if (type != EntryType.TASK) return null
    return Task(
        id = id,
        title = title,
        description = description.orEmpty(),
        dueDate = dueDate.toString(),
        isDone = isDone,
        time = time.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

internal fun EntryEntity.toHabitOrNull(): Habit? {
    if (type != EntryType.HABIT) return null
    return Habit(
        id = id,
        title = title,
        description = description.orEmpty(),
        isDone = isDone,
        time = time.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        recurrence = recurrence.toDomain(),
        streakCount = streakCount,
        lastCompletedDate = lastCompletedDate.toString(),
    )
}

internal fun EntryEntity.Recurrence?.toDomain(): Recurrence =
    when (this) {
        EntryEntity.Recurrence.DAILY -> Recurrence.Daily
        EntryEntity.Recurrence.WEEKLY -> Recurrence.Weekly
        EntryEntity.Recurrence.CUSTOM -> Recurrence.Custom
        else -> Recurrence.None
    }
