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

    private const val REMINDER_WORK_NAME_7AM = "daily_reminder_work_7am"
    private const val REMINDER_WORK_NAME_1PM = "daily_reminder_work_1pm"
    private const val REMINDER_WORK_NAME_7PM = "daily_reminder_work_7pm"

    fun setupDailyReminders(context: Context) {
        val workRequest7AM = createDailyReminderWorkRequest(7)
        val workRequest1PM = createDailyReminderWorkRequest(13)
        val workRequest7PM = createDailyReminderWorkRequest(19)

        WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork(
                REMINDER_WORK_NAME_7AM,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest7AM
            )
            enqueueUniquePeriodicWork(
                REMINDER_WORK_NAME_1PM,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest1PM
            )
            enqueueUniquePeriodicWork(
                REMINDER_WORK_NAME_7PM,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest7PM
            )
        }
    }

    fun cancelDailyReminders(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(REMINDER_WORK_NAME_7AM)
            cancelUniqueWork(REMINDER_WORK_NAME_1PM)
            cancelUniqueWork(REMINDER_WORK_NAME_7PM)
        }
    }

    fun triggerImmediateNotification(context: Context) {
        val immediateWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(immediateWorkRequest)
    }

    private fun createDailyReminderWorkRequest(hour: Int): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(hour), TimeUnit.MILLISECONDS)
            .build()
    }

    private fun calculateInitialDelay(targetHour: Int): Long {
        val now = System.currentTimeMillis()
        val targetTime = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return if (targetTime > now) targetTime - now else targetTime + TimeUnit.DAYS.toMillis(1) - now
    }
}

