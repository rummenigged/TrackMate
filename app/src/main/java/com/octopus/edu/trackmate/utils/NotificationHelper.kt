package com.octopus.edu.trackmate.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.octopus.edu.trackmate.MainActivity
import com.octopus.edu.trackmate.R
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context
    ) {
        fun showReminderNotification(
            entryId: String,
            title: String
        ) {
            val channelId = REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("entry_id", entryId)
                }

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val notification =
                NotificationCompat
                    .Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

            notificationManager.notify(entryId.hashCode(), notification)
        }
    }
