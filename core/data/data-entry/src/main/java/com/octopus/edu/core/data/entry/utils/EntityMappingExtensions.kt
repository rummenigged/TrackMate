package com.octopus.edu.core.data.entry.utils

import com.google.firebase.Timestamp
import com.octopus.edu.core.common.ReminderTimeCalculator.calculateReminderDelay
import com.octopus.edu.core.common.ReminderTimeCalculator.defaultTimeIfNull
import com.octopus.edu.core.common.ReminderTimeCalculator.getHabitInterval
import com.octopus.edu.core.common.toEpocMilliseconds
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

/**
     * Converts this persistence EntryEntity into its corresponding domain Entry.
     *
     * @return The domain Entry (Task or Habit) represented by this entity, or `null` if the entity's type is not supported or conversion is not possible.
     */
    fun EntryEntity.toDomain(): Entry? =
    when (type) {
        EntryType.TASK -> toTaskOrNull()
        EntryType.HABIT -> toHabitOrNull()
    }

/**
 * Converts this EntryEntity into a Task domain model when its type is TASK.
 *
 * Maps entity fields into a Task; if `dueDate` is absent it uses `LocalDate.now()`. `time` and `updatedAt` are preserved as nullable.
 *
 * @return the corresponding Task instance, or `null` if this entity's type is not TASK.
 */
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

/**
 * Convert this EntryEntity into a `Habit` domain model when the entity represents a habit.
 *
 * Maps entry fields to the `Habit` domain properties. If `startDate` is absent, uses the current date.
 *
 * @return A `Habit` constructed from this entity when `type` is `HABIT`, `null` otherwise.
 */
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

/**
     * Converts an EntryEntity.SyncStateEntity value to the corresponding domain [SyncState].
     *
     * @return `SyncState.PENDING` if the entity state is `PENDING`, `SyncState.FAILED` if the entity state is `FAILED`, `SyncState.SYNCED` otherwise.
     */
    internal fun EntryEntity.SyncStateEntity.toDomain(): SyncState =
    when (this) {
        EntryEntity.SyncStateEntity.PENDING -> SyncState.PENDING
        EntryEntity.SyncStateEntity.FAILED -> SyncState.FAILED
        else -> SyncState.SYNCED
    }

/**
     * Maps an optional persistence-layer recurrence value to the corresponding domain `Recurrence`.
     *
     * @return `Recurrence.Daily` if the entity value is `DAILY`, `Recurrence.Weekly` if `WEEKLY`,
     * `Recurrence.Custom` if `CUSTOM`, `Recurrence.None` otherwise.
     */
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

/**
     * Converts this domain Entry (Task or Habit) into its corresponding persistent EntryEntity.
     *
     * For a Habit the resulting entity includes recurrence and startDate; for a Task it includes dueDate.
     *
     * @return An EntryEntity with domain fields mapped to entity fields and timestamps represented as epoch milliseconds.
     */
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
                startDate = startDate.toEpocMilliseconds(),
                syncState = syncState.toEntity(),
            )

        is Task ->
            EntryEntity(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                dueDate = dueDate.toEpocMilliseconds(),
                time = time?.toEpochMilli(),
                createdAt = createdAt.toEpochMilli(),
                updatedAt = updatedAt?.toEpochMilli(),
                type = EntryType.TASK,
                syncState = syncState.toEntity(),
            )
    }

/**
     * Converts a domain `SyncState` value to the corresponding `EntryEntity.SyncStateEntity`.
     *
     * @return `EntryEntity.SyncStateEntity.PENDING` if this is `SyncState.PENDING`,
     * `EntryEntity.SyncStateEntity.SYNCED` if this is `SyncState.SYNCED`,
     * `EntryEntity.SyncStateEntity.FAILED` if this is `SyncState.FAILED`.
     */
    internal fun SyncState.toEntity(): EntryEntity.SyncStateEntity =
    when (this) {
        SyncState.PENDING -> EntryEntity.SyncStateEntity.PENDING
        SyncState.SYNCED -> EntryEntity.SyncStateEntity.SYNCED
        SyncState.FAILED -> EntryEntity.SyncStateEntity.FAILED
    }

/**
     * Converts this domain Entry (Habit or Task) into an EntryDto for transport or storage.
     *
     * The resulting DTO preserves common fields (id, title, description, isDone, time, type, createdAt, updatedAt)
     * and includes type-specific fields: Habit -> `recurrence` and `startDate`; Task -> `dueDate`.
     * Time, createdAt, and updatedAt are represented as Firebase `Timestamp` when present.
     *
     * @return An EntryDto representing this Entry with type-specific fields populated.
     */
    internal fun Entry.toDTO(): EntryDto =
    when (this) {
        is Habit ->
            EntryDto(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                time = time?.toEpochMilli()?.toInstant()?.let { Timestamp(it) },
                type = EntryType.HABIT.name,
                recurrence = recurrence.toDto(),
                startDate = startDate.toEpocMilliseconds(),
                createdAt = Timestamp(createdAt),
                updatedAt = updatedAt?.let { Timestamp(it) },
            )
        is Task ->
            EntryDto(
                id = id,
                title = title,
                description = description,
                isDone = isDone,
                time = time?.toEpochMilli()?.toInstant()?.let { Timestamp(it) },
                type = EntryType.TASK.name,
                dueDate = dueDate.toEpocMilliseconds(),
                createdAt = Timestamp(createdAt),
                updatedAt = updatedAt?.let { Timestamp(it) },
            )
    }

/**
     * Converts a recurrence value to the string used in DTOs.
     *
     * @return `"Custom"`, `"Daily"`, `"Weekly"`, or `"None"` corresponding to the recurrence; `"None"` is returned for `null` or unknown values.
     */
    internal fun Recurrence?.toDto(): String =
    when (this) {
        Recurrence.Custom -> "Custom"
        Recurrence.Daily -> "Daily"
        Recurrence.Weekly -> "Weekly"
        else -> "None"
    }

/**
     * Converts this DTO into its persistent EntryEntity representation.
     *
     * Timestamps are converted to epoch milliseconds. The `type` string "HABIT" maps to `EntryType.HABIT`
     * and any other value maps to `EntryType.TASK`. Recurrence strings "Custom", "Daily", and "Weekly"
     * map to the corresponding `EntryEntity.Recurrence` values; other recurrence values become `null`.
     * The resulting entity's `syncState` is set to `SYNCED`.
     *
     * @return An EntryEntity populated from this DTO with timestamps in epoch milliseconds and recurrence mapped to entity enums.
     */
    internal fun EntryDto.toEntity(): EntryEntity =
    EntryEntity(
        id = id,
        type = if (type == "HABIT") EntryType.HABIT else EntryType.TASK,
        title = title,
        description = description,
        isDone = isDone,
        dueDate = dueDate?.toInstant()?.toEpochMilli(),
        time = time?.toInstant()?.toEpochMilli(),
        createdAt = createdAt.toInstant().toEpochMilli(),
        updatedAt = updatedAt?.toInstant()?.toEpochMilli(),
        recurrence =
            when (recurrence) {
                "Custom" -> EntryEntity.Recurrence.CUSTOM
                "Daily" -> EntryEntity.Recurrence.DAILY
                "Weekly" -> EntryEntity.Recurrence.WEEKLY
                else -> null
            },
        startDate = startDate,
        syncState = EntryEntity.SyncStateEntity.SYNCED,
    )

/**
     * Builds a ReminderEntity for this entry with a generated id and timing based on the entry's time, date, and reminder offset.
     *
     * @return A ReminderEntity whose `id` is a new UUID string, `taskId` is the entry's id, `triggerAtMillis` is computed from the entry's time, the entry's date (dueDate for Task, startDate for Habit) and the reminder offset, `type` maps the entry's reminderType to ReminderType, `isRepeating` is `true` for Habit and `false` otherwise, and `repeatIntervalMillis` is the habit recurrence interval in milliseconds when this is a Habit and a reminder offset is present, or `null` otherwise.
     */
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

/**
     * Converts this persistence DeletedEntryEntity into a domain DeletedEntry.
     *
     * @return A DeletedEntry with the same `id` and `deletedAt` converted to an `Instant`.
     */
    internal fun DeletedEntryEntity.toDomain(): DeletedEntry =
    DeletedEntry(
        id = id,
        deletedAt = deletedAt.toInstant(),
    )

/**
     * Converts this domain DeletedEntry into a DeletedEntryDto for transport or persistence.
     *
     * The DTO will contain the same `id` and `deletedAt` represented as a `Timestamp`.
     *
     * @return A `DeletedEntryDto` with matching `id` and `deletedAt` as a `Timestamp`.
     */
    internal fun DeletedEntry.toDto(): DeletedEntryDto =
    DeletedEntryDto(
        id = id,
        deletedAt = Timestamp(deletedAt),
    )