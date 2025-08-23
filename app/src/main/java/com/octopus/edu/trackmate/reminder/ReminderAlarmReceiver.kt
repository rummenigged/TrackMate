package com.octopus.edu.trackmate.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.ui.reminder.ReminderActivity
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
                        val activityIntent =
                            Intent(context, ReminderActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra(ENTRY_ID_EXTRA, entryId)
                            }

                        notificationHelper.showAlarmNotification(
                            entryId,
                            result.data.title,
                            activityIntent,
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
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
