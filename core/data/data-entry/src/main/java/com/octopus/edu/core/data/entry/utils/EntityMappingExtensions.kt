package com.octopus.edu.core.data.entry.utils

import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun Long.toLocalDate() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun Long.toLocalTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime()

fun Long.toInstant() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toInstant()

private fun LocalTime.toEpochMilli(): Long =
    this
        .atDate(LocalDate.of(1970, 1, 1)) // reference epoch date
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

fun EntryEntity.toDomain(): Entry? =
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
        dueDate = dueDate?.toLocalDate() ?: LocalDate.now(),
        isDone = isDone,
        time = time?.toLocalTime(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt?.toInstant(),
    )
}

internal fun EntryEntity.toHabitOrNull(): Habit? {
    if (type != EntryType.HABIT) return null
    return Habit(
        id = id,
        title = title,
        description = description.orEmpty(),
        isDone = isDone,
        time = time?.toLocalTime(),
        startDate = startDate?.toLocalDate() ?: LocalDate.now(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt?.toInstant(),
        recurrence = recurrence.toDomain(),
        streakCount = streakCount,
        lastCompletedDate = lastCompletedDate?.toInstant(),
    )
}

internal fun EntryEntity.Recurrence?.toDomain(): Recurrence =
    when (this) {
        EntryEntity.Recurrence.DAILY -> Recurrence.Daily
        EntryEntity.Recurrence.WEEKLY -> Recurrence.Weekly
        EntryEntity.Recurrence.CUSTOM -> Recurrence.Custom
        else -> Recurrence.None
    }

private fun Recurrence?.toEntity(): EntryEntity.Recurrence? =
    when (this) {
        Recurrence.Daily -> EntryEntity.Recurrence.DAILY
        Recurrence.Weekly -> EntryEntity.Recurrence.WEEKLY
        Recurrence.Custom -> EntryEntity.Recurrence.CUSTOM
        else -> null
    }

internal fun Entry.toEntity() =
    when (this) {
        is Habit ->
            EntryEntity(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                time = time?.toEpochMilli(),
                createdAt = createdAt.toEpochMilli(),
                updatedAt = updatedAt?.toEpochMilli(),
                type = EntryType.HABIT,
                recurrence = recurrence.toEntity(),
                startDate = startDate.toEpochDay(),
            )

        is Task ->
            EntryEntity(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                dueDate = dueDate.toEpochDay(),
                time = time?.toEpochMilli(),
                createdAt = createdAt.toEpochMilli(),
                updatedAt = updatedAt?.toEpochMilli(),
                type = EntryType.TASK,
            )
    }
