package com.octopus.edu.core.domain.model

import java.time.Duration
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
    val dueDate: LocalDate? = null,
) : Entry() {
    companion object
}

sealed class Recurrence {
    object Daily : Recurrence()

    object Weekly : Recurrence()

    object Custom : Recurrence()

    object None : Recurrence()
}

sealed class Reminder {
    abstract val offset: Duration

    data object None : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object OnTime : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object FiveMinutesEarly : Reminder() {
        override val offset: Duration = Duration.ofMinutes(5)
    }

    data object ThirtyMinutesEarly : Reminder() {
        override val offset: Duration = Duration.ofMinutes(30)
    }

    data object OneHourEarly : Reminder() {
        override val offset: Duration = Duration.ofHours(1)
    }

    data object OnDay : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object DayEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(1)
    }

    data object TwoDaysEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(2)
    }

    data object ThreeDaysEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(3)
    }
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
    )

fun Habit.Companion.mockList(count: Int) = (1..count).map { mock(it.toString()) }
