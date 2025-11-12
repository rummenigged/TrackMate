package com.octopus.edu.core.data.entry

import com.google.firebase.Timestamp
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.common.toInstant
import com.octopus.edu.core.common.toLocalDate
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toDto
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class EntryMapperTest {
    @Test
    fun toTaskMapsEntryEntityToTaskCorrectly() {
        val entity =
            EntryEntity(
                id = "task-1",
                type = EntryEntity.EntryType.TASK,
                title = "Finish report",
                description = "Due tomorrow",
                isDone = false,
                dueDate = LocalDate.of(2025, 6, 30).toEpochMilli(),
                recurrence = null,
                streakCount = null,
                lastCompletedDate = null,
                createdAt = LocalDate.of(2025, 6, 30).toEpochMilli(),
                updatedAt = LocalDate.of(2025, 7, 20).toEpochMilli(),
                syncState = EntryEntity.SyncStateEntity.SYNCED,
            )

        val task = entity.toTaskOrNull()

        assertTrue(task is Task)
        assertEquals("task-1", task?.id)
        assertEquals("Finish report", task?.title)
        assertEquals("Due tomorrow", task?.description)
        assertEquals(entity.dueDate?.toLocalDate(), task?.dueDate)
        assertFalse(task?.isDone == true)
        assertEquals(entity.createdAt.toInstant(), task?.createdAt)
        assertEquals(entity.updatedAt?.toInstant(), task?.updatedAt)
        assertEquals(entity.syncState, task?.syncState?.toEntity())
    }

    @Test
    fun toHabitMapsEntryEntityToHabitCorrectly() {
        val entity =
            EntryEntity(
                id = "habit-1",
                type = EntryEntity.EntryType.HABIT,
                title = "Drink water",
                description = null,
                isDone = true,
                dueDate = null,
                recurrence = EntryEntity.Recurrence.DAILY,
                streakCount = 5,
                lastCompletedDate = LocalDate.of(2025, 6, 27).toEpochMilli(),
                createdAt = 100L,
                updatedAt = 200L,
                syncState = EntryEntity.SyncStateEntity.SYNCED,
            )

        val habit = entity.toHabitOrNull()

        assertTrue(habit is Habit)
        assertEquals("habit-1", habit?.id)
        assertEquals("Drink water", habit?.title)
        assertEquals("", habit?.description) // fallback for null
        assertTrue(habit?.isDone == true)
        assertEquals(Recurrence.Daily, habit?.recurrence)
        assertEquals(5, habit?.streakCount)
        assertEquals(entity.lastCompletedDate?.toInstant(), habit?.lastCompletedDate)
        assertEquals(entity.createdAt.toInstant(), habit?.createdAt)
        assertEquals(entity.updatedAt?.toInstant(), habit?.updatedAt)
        assertEquals(entity.syncState, habit?.syncState?.toEntity())
    }

    @Test
    fun toTaskReturnsNullIfTypeIsNotTask() {
        val invalid =
            EntryEntity(
                id = "wrong",
                type = EntryEntity.EntryType.HABIT,
                title = "This is not a task",
                description = "fail",
                isDone = true,
                dueDate = null,
                recurrence = EntryEntity.Recurrence.DAILY,
                streakCount = 1,
                lastCompletedDate = LocalDate.now().toEpochMilli(),
                createdAt = 1,
                updatedAt = null,
                syncState = EntryEntity.SyncStateEntity.SYNCED,
            )

        assertEquals(invalid.toTaskOrNull(), null) //
    }

    @Test
    fun toHabit_returnsNullIfTypeIsNotHabit() {
        val invalid =
            EntryEntity(
                id = "wrong2",
                type = EntryEntity.EntryType.TASK,
                title = "This is not a habit",
                description = "fail",
                isDone = false,
                dueDate = LocalDate.now().toEpochMilli(),
                recurrence = null,
                streakCount = null,
                lastCompletedDate = null,
                createdAt = 1,
                updatedAt = null,
                syncState = EntryEntity.SyncStateEntity.SYNCED,
            )

        assertEquals(invalid.toHabitOrNull(), null)
    }

    @Test
    fun taskToEntityMapsCorrectly() {
        val task =
            Task(
                id = "task-1",
                title = "A new task",
                description = "A description",
                isDone = true,
                dueDate = LocalDate.of(2025, 1, 1),
                time = LocalTime.of(10, 30),
                createdAt = Instant.ofEpochMilli(1000L),
                updatedAt = Instant.ofEpochMilli(2000L),
                syncState = SyncState.PENDING,
            )

        val entity = task.toEntity()

        assertEquals("task-1", entity.id)
        assertEquals("A new task", entity.title)
        assertEquals("A description", entity.description)
        assertTrue(entity.isDone)
        assertEquals(EntryEntity.EntryType.TASK, entity.type)
        assertEquals(EntryEntity.SyncStateEntity.PENDING, entity.syncState)
        assertEquals(LocalDate.of(2025, 1, 1).toEpochMilli(), entity.dueDate)
        assertEquals(LocalTime.of(10, 30).toEpochMilli(), entity.time)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun habitToEntityMapsCorrectly() {
        val habit =
            Habit(
                id = "habit-1",
                title = "A new habit",
                description = "A description",
                isDone = false,
                startDate = LocalDate.of(2025, 1, 1),
                time = LocalTime.of(11, 0),
                createdAt = Instant.ofEpochMilli(3000L),
                updatedAt = Instant.ofEpochMilli(4000L),
                syncState = SyncState.SYNCED,
                recurrence = Recurrence.Weekly,
                streakCount = 10,
                lastCompletedDate = Instant.ofEpochMilli(5000L),
            )

        val entity = habit.toEntity()

        assertEquals("habit-1", entity.id)
        assertEquals("A new habit", entity.title)
        assertEquals("A description", entity.description)
        assertFalse(entity.isDone)
        assertEquals(EntryEntity.EntryType.HABIT, entity.type)
        assertEquals(EntryEntity.Recurrence.WEEKLY, entity.recurrence)
        assertEquals(EntryEntity.SyncStateEntity.SYNCED, entity.syncState)
        assertEquals(LocalDate.of(2025, 1, 1).toEpochMilli(), entity.startDate)
        assertEquals(LocalTime.of(11, 0).toEpochMilli(), entity.time)
        assertEquals(3000L, entity.createdAt)
        assertEquals(4000L, entity.updatedAt)
        assertEquals(null, entity.streakCount)
        assertEquals(null, entity.lastCompletedDate)
    }

    @Test
    fun syncStateMappingsWorkCorrectly() {
        assertEquals(SyncState.PENDING, EntryEntity.SyncStateEntity.PENDING.toDomain())
        assertEquals(SyncState.SYNCED, EntryEntity.SyncStateEntity.SYNCED.toDomain())
        assertEquals(SyncState.FAILED, EntryEntity.SyncStateEntity.FAILED.toDomain())
        assertEquals(SyncState.SYNCED, EntryEntity.SyncStateEntity.CONFLICT.toDomain()) // CONFLICT maps to SYNCED

        assertEquals(EntryEntity.SyncStateEntity.PENDING, SyncState.PENDING.toEntity())
        assertEquals(EntryEntity.SyncStateEntity.SYNCED, SyncState.SYNCED.toEntity())
        assertEquals(EntryEntity.SyncStateEntity.FAILED, SyncState.FAILED.toEntity())
    }

    @Test
    fun recurrenceMappingsWorkCorrectly() {
        assertEquals(Recurrence.Daily, EntryEntity.Recurrence.DAILY.toDomain())
        assertEquals(Recurrence.Weekly, EntryEntity.Recurrence.WEEKLY.toDomain())
        assertEquals(Recurrence.Custom, EntryEntity.Recurrence.CUSTOM.toDomain())
        assertEquals(Recurrence.None, (null as EntryEntity.Recurrence?).toDomain())
    }

    @Test
    fun taskToDtoMapsCorrectly() {
        val task =
            Task(
                id = "task-dto-1",
                title = "Task DTO",
                description = "Description DTO",
                isDone = true,
                dueDate = LocalDate.of(2025, 2, 2),
                time = LocalTime.of(12, 0),
                createdAt = Instant.ofEpochMilli(6000L),
                updatedAt = Instant.ofEpochMilli(7000L),
                syncState = SyncState.PENDING,
            )

        val dto = task.toDTO()

        assertEquals("task-dto-1", dto.id)
        assertEquals("Task DTO", dto.title)
        assertEquals("Description DTO", dto.description)
        assertTrue(dto.done)
        assertEquals("TASK", dto.type)
        assertEquals(LocalDate.of(2025, 2, 2).toEpochMilli(), dto.dueDate)
        assertEquals(LocalTime.of(12, 0).toEpochMilli(), dto.time?.toInstant()?.toEpochMilli())
        assertEquals(6000L, dto.createdAt?.toInstant()?.toEpochMilli())
        assertEquals(7000L, dto.updatedAt?.toInstant()?.toEpochMilli())
    }

    @Test
    fun dtoToEntityMapsCorrectly() {
        val now = Timestamp.now()
        val dto =
            EntryDto(
                id = "dto-entity-1",
                title = "DTO to Entity",
                description = "DTO desc",
                done = false,
                type = "TASK",
                dueDate = 8000L,
                time = Timestamp(Instant.ofEpochMilli(9000L)),
                createdAt = now,
                updatedAt = now,
            )

        val entity = dto.toEntity()

        assertEquals("dto-entity-1", entity.id)
        assertEquals("DTO to Entity", entity.title)
        assertEquals("DTO desc", entity.description)
        assertFalse(entity.isDone)
        assertEquals(EntryEntity.EntryType.TASK, entity.type)
        assertEquals(8000L, entity.dueDate)
        assertEquals(9000L, entity.time)
        assertEquals(now.toInstant().toEpochMilli(), entity.createdAt)
        assertEquals(now.toInstant().toEpochMilli(), entity.updatedAt)
        assertEquals(EntryEntity.SyncStateEntity.SYNCED, entity.syncState) // DTOs are always considered SYNCED
    }

    @Test
    fun doneEntryMappingsWorkCorrectly() {
        val doneEntry =
            DoneEntry(
                id = "done-1",
                date = LocalDate.of(2025, 3, 3),
                doneAt = Instant.ofEpochMilli(10000L),
            )

        val entity =
            DoneEntryEntity(
                entryId = "done-1",
                entryDate = LocalDate.of(2025, 3, 3).toEpochMilli(),
                doneAt = 10000L,
                isConfirmed = true,
                syncState = EntryEntity.SyncStateEntity.PENDING,
            )

        val dto = doneEntry.toDto()
        val domainFromEntity = entity.toDomain()

        // Domain to DTO
        assertEquals(doneEntry.id, dto.id)
        assertEquals(Timestamp(doneEntry.date.toEpochMilli().toInstant()), dto.date)
        assertEquals(Timestamp(doneEntry.doneAt), dto.doneAt)

        // Entity to Domain
        assertEquals(entity.entryId, domainFromEntity.id)
        assertEquals(entity.entryDate.toLocalDate(), domainFromEntity.date)
        assertEquals(entity.doneAt.toInstant(), domainFromEntity.doneAt)
    }

    @Test
    fun deletedEntryMappingsWorkCorrectly() {
        val deletedEntry =
            DeletedEntry(
                id = "deleted-1",
                deletedAt = Instant.ofEpochMilli(11000L),
            )

        val entity =
            DeletedEntryEntity(
                id = "deleted-1",
                deletedAt = 11000L,
                syncState = EntryEntity.SyncStateEntity.PENDING,
            )

        val dto = deletedEntry.toDto()
        val domainFromEntity = entity.toDomain()

        // Domain to DTO
        assertEquals(deletedEntry.id, dto.id)
        assertEquals(Timestamp(deletedEntry.deletedAt), dto.deletedAt)

        // Entity to Domain
        assertEquals(entity.id, domainFromEntity.id)
        assertEquals(entity.deletedAt.toInstant(), domainFromEntity.deletedAt)
    }
}
