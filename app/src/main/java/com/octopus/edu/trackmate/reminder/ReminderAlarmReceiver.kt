package com.octopus.edu.trackmate.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.toLocalDate
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.ReminderStrategyFactory
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderStrategyFactory: ReminderStrategyFactory

    private val job = SupervisorJob()

    private val coroutineScope by lazy { CoroutineScope(dispatcherProvider.io + job) }

    override fun onReceive(
        context: Context?,
        intent: Intent?
    ) {
        if (context == null) return
        val entryId = intent?.getStringExtra(ENTRY_ID_EXTRA) ?: return

        val pendingResult = goAsync()

        coroutineScope.launch {
            try {
                when (val result = entryRepository.getEntryById(entryId)) {
                    is ResultOperation.Error -> {}
                    is ResultOperation.Success -> {
                        processSuccessfulEntry(result.data, intent)
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    ReminderAlarmReceiver::class.simpleName,
                    "Exception in onReceive for " +
                        "entry $entryId",
                    e,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun processSuccessfulEntry(
        entry: Entry,
        intent: Intent?
    ) {
        notificationHelper.showAlarmNotification(entry.id, entry.title)

        if (entry !is Habit) return

        intent?.let {
            val intervalMillis = it.getLongExtra(ALARM_INTERVAL_EXTRA, 0L)
            if (it.action == ACTION_HABIT_REMINDER) {
                try {
                    val nextOccurrenceTimeMillis = System.currentTimeMillis() + intervalMillis
                    val nextEntry =
                        entry.copy(
                            startDate = nextOccurrenceTimeMillis.toLocalDate(),
                        )

                    reminderStrategyFactory
                        .getStrategy(
                            entry,
                            ReminderType.ALARM,
                        )?.schedule(nextEntry)
                } catch (e: NoSuchElementException) {
                    Log.e(
                        ReminderAlarmReceiver::class.simpleName,
                        "Failed to create next entry " +
                            "for rescheduling. Ensure 'Entry' is a data class with a 'copy()' " +
                            "method and a time field (e.g., 'dueDate').",
                        e,
                    )
                } catch (e: Exception) {
                    Log.e(
                        ReminderAlarmReceiver::class.simpleName,
                        "Error rescheduling habit alarm " +
                            "for entry ${entry.id}",
                        e,
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_HABIT_REMINDER = "ACTION_HABIT_REMINDER"
        const val ACTION_TASK_REMINDER = "ACTION_TASK_REMINDER"
        const val ALARM_INTERVAL_EXTRA = "ALARM_INTERVAL_EXTRA"

        fun getPendingIntent(
            context: Context,
            entryId: String,
            intervalMillis: Long = 0L,
            flags: Int =
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        ): PendingIntent {
            val intent =
                Intent(context, ReminderAlarmReceiver::class.java).apply {
                    this.putExtra(ENTRY_ID_EXTRA, entryId)
                    if (intervalMillis > 0) {
                        action = ACTION_HABIT_REMINDER
                        putExtra(ALARM_INTERVAL_EXTRA, intervalMillis)
                    } else {
                        action = ACTION_TASK_REMINDER
                    }
                }

            return PendingIntent.getBroadcast(
                context,
                (entryId + ACTION_HABIT_REMINDER).hashCode(),
                intent,
                flags,
            )
        }
    }
}
