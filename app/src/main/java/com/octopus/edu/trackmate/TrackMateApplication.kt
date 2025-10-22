package com.octopus.edu.trackmate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.octopus.edu.trackmate.logger.CrashReportingTree
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA
import com.octopus.edu.trackmate.sync.EntrySyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TrackMateApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: EntrySyncManager

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .setWorkerFactory(workerFactory)
                .build()

    /**
     * Initializes application-level services and configuration on process start.
     *
     * Creates the entry reminder notification channel, starts the entry synchronization manager,
     * and configures Timber logging (debug tree in debug builds, crash-reporting tree otherwise).
     */
    override fun onCreate() {
        super.onCreate()
        createEntryReminderNotificationChannel(this)

        syncManager.start()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
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