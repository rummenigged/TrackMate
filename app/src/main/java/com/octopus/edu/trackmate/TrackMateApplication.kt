package com.octopus.edu.trackmate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TrackMateApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        createEntryReminderNotificationChannel(this)
    }

    private fun createEntryReminderNotificationChannel(context: Context) {
        val channel =
            NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.reminder_channel_description)
            }

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
