package org.bangkit.kiddos_android.worker

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderManager {

    private const val REMINDER_WORK_NAME = "daily_reminder_work"

    fun setupDailyReminder(context: Context) {
        val workRequest = createDailyReminderWorkRequest()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    private fun createDailyReminderWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .setConstraints(createConstraints())
            .build()
    }

    private fun calculateInitialDelay(): Long {
        val now = System.currentTimeMillis()
        val targetTime = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return if (targetTime > now) targetTime - now else targetTime + TimeUnit.DAYS.toMillis(1) - now
    }

    private fun createConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
