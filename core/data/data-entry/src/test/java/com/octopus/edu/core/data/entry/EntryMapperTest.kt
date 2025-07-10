package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toInstant
import com.octopus.edu.core.data.entry.utils.toLocalDate
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

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
                dueDate = LocalDate.of(2025, 6, 30).toEpochDay(),
                recurrence = null,
                streakCount = null,
                lastCompletedDate = null,
                createdAt = LocalDate.of(2025, 6, 30).toEpochDay(),
                updatedAt = LocalDate.of(2025, 7, 20).toEpochDay(),
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
                lastCompletedDate = LocalDate.of(2025, 6, 27).toEpochDay(),
                createdAt = 100L,
                updatedAt = 200L,
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
                lastCompletedDate = LocalDate.now().toEpochDay(),
                createdAt = 1,
                updatedAt = null,
            )

        assertEquals(invalid.toTaskOrNull(), null) //
    }

    @Test()
    fun toHabit_returnsNullIfTypeIsNotHabit() {
        val invalid =
            EntryEntity(
                id = "wrong2",
                type = EntryEntity.EntryType.TASK,
                title = "This is not a habit",
                description = "fail",
                isDone = false,
                dueDate = LocalDate.now().toEpochDay(),
                recurrence = null,
                streakCount = null,
                lastCompletedDate = null,
                createdAt = 1,
                updatedAt = null,
            )

        assertEquals(invalid.toHabitOrNull(), null)
    }
}
