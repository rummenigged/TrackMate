package com.octopus.edu.core.domain.model

import com.octopus.edu.core.domain.scheduler.ReminderType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
sealed class Entry {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val isDone: Boolean
    abstract val time: LocalTime?
    abstract val createdAt: Instant
    abstract val updatedAt: Instant?
    abstract val reminder: Reminder?
    abstract val reminderType: ReminderType?
}

@OptIn(ExperimentalTime::class)
data class Task(
    override val id: String,
    override val title: String,
    override val description: String,
    override val isDone: Boolean,
    override val time: LocalTime?,
    override val createdAt: Instant,
    override val updatedAt: Instant? = null,
    override val reminder: Reminder? = null,
    override val reminderType: ReminderType? = null,
    val dueDate: LocalDate,
) : Entry() {
    companion object
}

@OptIn(ExperimentalTime::class)
data class Habit(
    override val id: String,
    override val title: String,
    override val description: String,
    override val isDone: Boolean,
    override val time: LocalTime?,
    override val createdAt: Instant,
    override val updatedAt: Instant? = null,
    override val reminder: Reminder? = null,
    override val reminderType: ReminderType? = null,
    val startDate: LocalDate,
    val recurrence: Recurrence?,
    val streakCount: Int? = null,
    val lastCompletedDate: Instant? = null,
) : Entry() {
    companion object
}

@OptIn(ExperimentalTime::class)
fun Task.Companion.mock(id: String) =
    Task(
        id = id,
        title = "Task $id",
        description = "Description $id",
        isDone = id.toInt() % 2 == 0,
        time = if (id.toInt() % 2 == 0) LocalTime.now() else null,
        createdAt = Instant.now(),
        reminderType = ReminderType.NOTIFICATION,
        dueDate = LocalDate.now().plusDays(1),
    )

fun Task.Companion.mockList(count: Int) = (1..count).map { mock(it.toString()) }

@OptIn(ExperimentalTime::class)
fun Habit.Companion.mock(id: String) =
    Habit(
        id = id,
        title = "Habit $id",
        description = "Description $id",
        isDone = id.toInt() % 2 == 0,
        time = if (id.toInt() % 2 == 0) LocalTime.now() else null,
        createdAt = Instant.now(),
        lastCompletedDate = Instant.now(),
        recurrence = Recurrence.Daily,
        streakCount = 0,
        reminderType = ReminderType.NOTIFICATION,
        startDate = LocalDate.now(),
    )

fun Habit.Companion.mockList(count: Int) = (1..count).map { mock(it.toString()) }

fun Habit.appliesTo(date: LocalDate): Boolean {
    if (date.isBefore(startDate)) return false

    return when (recurrence) {
        Recurrence.Daily -> true
        Recurrence.Weekly -> startDate.dayOfWeek == date.dayOfWeek
        Recurrence.Custom,
        Recurrence.None,
        null -> startDate == date
    }
}
