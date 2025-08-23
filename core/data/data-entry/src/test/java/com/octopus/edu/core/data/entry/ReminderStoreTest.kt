package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.ReminderType
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.store.ReminderStoreImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderStoreTest {
    private lateinit var reminderDao: ReminderDao
    private lateinit var reminderStore: ReminderStore

    private val reminder =
        ReminderEntity(
            id = "1",
            taskId = "task_1",
            triggerAtMillis = 123456789L,
            type = ReminderType.NOTIFICATION,
        )

    @Before
    fun setUp() {
        reminderDao = mockk(relaxed = true)
        reminderStore = ReminderStoreImpl(reminderDao)
    }

    @Test
    fun `saveReminder delegates to dao insert`() =
        runTest {
            // When
            reminderStore.saveReminder(reminder)

            // Then
            coVerify { reminderDao.insert(reminder) }
        }

    @Test
    fun `getReminderByEntryId returns reminder when dao returns value`() =
        runTest {
            // Given
            coEvery { reminderDao.getReminderByEntryId("task_1") } returns reminder

            // When
            val result = reminderStore.getReminderByEntryId("task_1")

            // Then
            assertEquals(reminder, result)
            coVerify { reminderDao.getReminderByEntryId("task_1") }
        }

    @Test
    fun `getReminderByEntryId returns null when dao returns null`() =
        runTest {
            // Given
            coEvery { reminderDao.getReminderByEntryId("task_2") } returns null

            // When
            val result = reminderStore.getReminderByEntryId("task_2")

            // Then
            assertNull(result)
            coVerify { reminderDao.getReminderByEntryId("task_2") }
        }

    @Test
    fun `saveReminder with type NOTIFICATION delegates correctly`() =
        runTest {
            val notificationReminder = reminder.copy(type = ReminderType.NOTIFICATION)

            reminderStore.saveReminder(notificationReminder)

            coVerify { reminderDao.insert(notificationReminder) }
        }

    @Test
    fun `saveReminder with type ALARM delegates correctly`() =
        runTest {
            val alarmReminder = reminder.copy(type = ReminderType.ALARM)

            reminderStore.saveReminder(alarmReminder)

            coVerify { reminderDao.insert(alarmReminder) }
        }

    @Test
    fun `getReminderByEntryId returns reminder with type NOTIFICATION`() =
        runTest {
            val notificationReminder = reminder.copy(type = ReminderType.NOTIFICATION)
            coEvery { reminderDao.getReminderByEntryId("task_1") } returns notificationReminder

            val result = reminderStore.getReminderByEntryId("task_1")

            assertEquals(ReminderType.NOTIFICATION, result?.type)
        }

    @Test
    fun `getReminderByEntryId returns reminder with type ALARM`() =
        runTest {
            val alarmReminder = reminder.copy(type = ReminderType.ALARM)
            coEvery { reminderDao.getReminderByEntryId("task_2") } returns alarmReminder

            val result = reminderStore.getReminderByEntryId("task_2")

            assertEquals(ReminderType.ALARM, result?.type)
        }
}
