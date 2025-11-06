package com.octopus.edu.core.data.entry.utils

import com.google.firebase.Timestamp
import com.octopus.edu.core.common.ReminderTimeCalculator.calculateReminderDelay
import com.octopus.edu.core.common.ReminderTimeCalculator.defaultTimeIfNull
import com.octopus.edu.core.common.ReminderTimeCalculator.getHabitInterval
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.common.toInstant
import com.octopus.edu.core.common.toLocalDate
import com.octopus.edu.core.common.toLocalTime
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.ReminderType
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderType.NOTIFICATION
import java.time.LocalDate
import java.util.UUID

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
        syncState = syncState.toDomain(),
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
        syncState = syncState.toDomain(),
    )
}

internal fun EntryEntity.SyncStateEntity.toDomain(): SyncState =
    when (this) {
        EntryEntity.SyncStateEntity.PENDING -> SyncState.PENDING
        EntryEntity.SyncStateEntity.FAILED -> SyncState.FAILED
        else -> SyncState.SYNCED
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
                type = EntryType.HABIT,
                recurrence = recurrence.toEntity(),
                syncState = syncState.toEntity(),
                startDate = startDate.toEpochMilli(),
                time = time?.toEpochMilli(),
                createdAt = createdAt.toEpochMilli(),
                updatedAt = updatedAt?.toEpochMilli(),
            )

        is Task ->
            EntryEntity(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                type = EntryType.TASK,
                syncState = syncState.toEntity(),
                dueDate = dueDate.toEpochMilli(),
                time = time?.toEpochMilli(),
                createdAt = createdAt.toEpochMilli(),
                updatedAt = updatedAt?.toEpochMilli(),
            )
    }

internal fun SyncState.toEntity(): EntryEntity.SyncStateEntity =
    when (this) {
        SyncState.PENDING -> EntryEntity.SyncStateEntity.PENDING
        SyncState.SYNCED -> EntryEntity.SyncStateEntity.SYNCED
        SyncState.FAILED -> EntryEntity.SyncStateEntity.FAILED
    }

internal fun Entry.toDTO(): EntryDto =
    when (this) {
        is Habit ->
            EntryDto(
                id = id,
                title = title,
                description = description,
                done = isDone,
                type = EntryType.HABIT.name,
                recurrence = recurrence.toDto(),
                time = time?.toEpochMilli()?.toInstant()?.let { Timestamp(it) },
                startDate = startDate.toEpochMilli(),
                createdAt = Timestamp(createdAt),
                updatedAt = updatedAt?.let { Timestamp(it) },
            )
        is Task ->
            EntryDto(
                id = id,
                title = title,
                description = description,
                done = isDone,
                type = EntryType.TASK.name,
                time = time?.toEpochMilli()?.toInstant()?.let { Timestamp(it) },
                dueDate = dueDate.toEpochMilli(),
                createdAt = Timestamp(createdAt),
                updatedAt = updatedAt?.let { Timestamp(it) },
            )
    }

internal fun Recurrence?.toDto(): String =
    when (this) {
        Recurrence.Custom -> "Custom"
        Recurrence.Daily -> "Daily"
        Recurrence.Weekly -> "Weekly"
        else -> "None"
    }

internal fun EntryDto.toEntity(): EntryEntity =
    EntryEntity(
        id = id,
        type = if (type == "HABIT") EntryType.HABIT else EntryType.TASK,
        title = title,
        description = description,
        isDone = done,
        recurrence =
            when (recurrence) {
                "Custom" -> EntryEntity.Recurrence.CUSTOM
                "Daily" -> EntryEntity.Recurrence.DAILY
                "Weekly" -> EntryEntity.Recurrence.WEEKLY
                else -> null
            },
        syncState = EntryEntity.SyncStateEntity.SYNCED,
        startDate = startDate,
        dueDate = dueDate,
        time = time?.toInstant()?.toEpochMilli(),
        createdAt = createdAt?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toInstant()?.toEpochMilli(),
    )

internal fun Entry.getReminderAsEntity(): ReminderEntity =
    ReminderEntity(
        id = UUID.randomUUID().toString(),
        taskId = id,
        triggerAtMillis =
            calculateReminderDelay(
                time = defaultTimeIfNull(time),
                date = if (this is Task) dueDate else (this as Habit).startDate,
                reminderOffset = reminder?.offset ?: Reminder.None.offset,
            ).toMillis(),
        type = if (reminderType == NOTIFICATION) ReminderType.NOTIFICATION else ReminderType.ALARM,
        isRepeating = this is Habit,
        repeatIntervalMillis =
            if (this is Habit) {
                reminder
                    ?.offset
                    ?.let {
                        getHabitInterval(recurrence, it)
                    }?.toMillis()
            } else {
                null
            },
    )

internal fun DeletedEntryEntity.toDomain(): DeletedEntry =
    DeletedEntry(
        id = id,
        deletedAt = deletedAt.toInstant(),
    )

internal fun DeletedEntry.toDto(): DeletedEntryDto =
    DeletedEntryDto(
        id = id,
        deletedAt = Timestamp(deletedAt),
    )
