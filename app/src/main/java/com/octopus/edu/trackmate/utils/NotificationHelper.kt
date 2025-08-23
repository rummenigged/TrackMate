package com.octopus.edu.trackmate.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.octopus.edu.trackmate.R // Assuming R is correctly imported
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Good practice for a helper like this
class NotificationHelper
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context
    ) {
        val notificationManager by lazy {
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        fun showReminderNotification(
            id: Int,
            title: String,
            intent: Intent
        ) {
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val notification =
                NotificationCompat
                    .Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

            notificationManager.notify(id, notification)
        }

        fun showAlarmNotification(
            entryId: String,
            title: String,
            intent: Intent
        ) {
            val fullScreenPendingIntent =
                PendingIntent.getActivity(
                    context,
                    entryId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationBuilder =
                NotificationCompat
                    .Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                notificationManager.canUseFullScreenIntent()
            ) {
                notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            } else {
                notificationBuilder.setContentIntent(fullScreenPendingIntent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (notificationManager.canUseFullScreenIntent()) {
                    notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
                } else {
                    notificationBuilder.setContentIntent(fullScreenPendingIntent)
                }
            } else {
                notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            }

            notificationManager.notify(entryId.hashCode(), notificationBuilder.build())
        }
    }
