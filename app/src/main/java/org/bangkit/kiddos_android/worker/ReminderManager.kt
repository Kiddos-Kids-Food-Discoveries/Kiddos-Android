package org.bangkit.kiddos_android.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderManager {

    private const val REMINDER_WORK_NAME = "daily_reminder_work"

    fun setupDailyReminder(context: Context) {
        val workRequest = createDailyReminderWorkRequest()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun triggerImmediateNotification(context: Context) {
        val immediateWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(immediateWorkRequest)
    }

    private fun createDailyReminderWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
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
}
