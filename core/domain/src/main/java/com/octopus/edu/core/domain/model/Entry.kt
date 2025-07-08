package com.octopus.edu.core.domain.model

sealed class Entry {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val isDone: Boolean
    abstract val time: String
    abstract val createdAt: String
    abstract val updatedAt: String?
}

data class Task(
    override val id: String,
    override val title: String,
    override val description: String,
    override val isDone: Boolean,
    override val time: String,
    override val createdAt: String,
    override val updatedAt: String? = null,
    val dueDate: String? = null,
) : Entry() {
    companion object
}

sealed class Recurrence {
    object Daily : Recurrence()

    object Weekly : Recurrence()

    object Custom : Recurrence()

    object None : Recurrence()
}

data class Habit(
    override val id: String,
    override val title: String,
    override val description: String,
    override val isDone: Boolean,
    override val time: String,
    override val createdAt: String,
    override val updatedAt: String? = null,
    val recurrence: Recurrence?,
    val streakCount: Int? = null,
    val lastCompletedDate: String? = null,
) : Entry() {
    companion object
}

fun Task.Companion.mock(id: String) =
    Task(
        id = id,
        title = "Task $id",
        description = "Description $id",
        isDone = id.toInt() % 2 == 0,
        time = "All Day",
        createdAt = "2022-01-01T00:00:00.000Z",
        updatedAt = "2022-01-01T00:00:00.000Z",
        dueDate = "2022-01-01T00:00:00.000Z",
    )

fun Task.Companion.mockList(count: Int) = (1..count).map { mock(it.toString()) }

fun Habit.Companion.mock(id: String) =
    Habit(
        id = id,
        title = "Habit $id",
        description = "Description $id",
        isDone = id.toInt() % 2 == 0,
        time = "All Day",
        createdAt = "2022-01-01T00:00:00.000Z",
        updatedAt = "2022-01-01T00:00:00.000Z",
        lastCompletedDate = "2022-01-01T00:00:00.000Z",
        recurrence = Recurrence.Daily,
        streakCount = 0,
    )

fun Habit.Companion.mockList(count: Int) = (1..count).map { mock(it.toString()) }
