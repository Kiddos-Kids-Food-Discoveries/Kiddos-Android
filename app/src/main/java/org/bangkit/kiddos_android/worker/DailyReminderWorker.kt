package org.bangkit.kiddos_android.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.ui.activity.MainActivity

class DailyReminderWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            showNotification()
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Error showing notification: ${e.message}")
            Result.failure()
        }
    }

    private fun showNotification() {
        createNotificationChannel()

        // Check if the app has the necessary permission to post notifications
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, "daily_reminder_channel")
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Daily Reminder")
                .setContentText("This is your daily reminder notification!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(1, builder.build())
            }
        } else {
            Log.e("DailyReminderWorker", "Notification permission not granted")
            // Optionally, you can inform the user or handle this case appropriately
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminder Channel"
            val descriptionText = "Channel for daily reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("daily_reminder_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
