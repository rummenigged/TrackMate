package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class EntryDaoTest {
    private lateinit var database: TrackMateDatabase
    private lateinit var dao: EntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TrackMateDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.entryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveHabitsAndTasks() =
        runTest {
            // Given
            val habit =
                EntryEntity(
                    id = "h1",
                    type = EntryEntity.EntryType.HABIT,
                    title = "Drink Water",
                    description = "8 glasses a day",
                    isDone = false,
                    dueDate = null,
                    recurrence = EntryEntity.Recurrence.DAILY,
                    streakCount = 5,
                    lastCompletedDate = LocalDate.now().toEpochDay(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null,
                )

            val task =
                EntryEntity(
                    id = "t1",
                    type = EntryEntity.EntryType.TASK,
                    title = "Finish Report",
                    description = "Due by Friday",
                    isDone = false,
                    dueDate = LocalDate.now().plusDays(2).toEpochDay(),
                    recurrence = null,
                    streakCount = null,
                    lastCompletedDate = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null,
                )

            // When
            dao.insert(habit)
            dao.insert(task)

            val habits = dao.getHabits()
            val tasks = dao.getTasks()

            // Then
            assert(habits.size == 1)
            assert(tasks.size == 1)

            assert(habits[0].id == "h1")
            assert(tasks[0].id == "t1")
        }
}
