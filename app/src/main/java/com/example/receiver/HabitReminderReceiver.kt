package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_IS_MORNING_MESSAGE = "extra_is_morning_message"
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_MORNING_MESSAGE = "extra_morning_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!NotificationHelper.hasNotificationPermission(context)) return

        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0L)
        val isMorningMessage = intent.getBooleanExtra(EXTRA_IS_MORNING_MESSAGE, false)

        NotificationHelper.createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title: String
        val body: String

        if (isMorningMessage) {
            val userName = intent.getStringExtra(EXTRA_USER_NAME)?.ifBlank { "Friend" } ?: "Friend"
            val customMsg = intent.getStringExtra(EXTRA_MORNING_MESSAGE).orEmpty()
            title = "Good Morning, $userName!"
            body = customMsg.ifBlank {
                "Ready for a great day? Complete at least 70% of your habits today to extend your streak!"
            }
        } else {
            val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Your Habit"
            title = "Habit Reminder: $habitTitle"
            body = "Keep your streak alive! Take a moment to complete your habit."
        }

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(habitId.toInt(), notification)
    }
}
