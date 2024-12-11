package org.bangkit.kiddos_android.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.databinding.ActivitySettingBinding
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.worker.ReminderManager

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var userPreference: UserPreference

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            ReminderManager.setupDailyReminders(this)
            showImmediateNotification()
            showNotificationToast(true)
        } else {
            Toast.makeText(this, "Dibutuhkan izin notifikasi untuk mengaktifkan pengingat harian.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this)
        binding.switchNotif.apply {
            lifecycleScope.launch {
                isChecked = userPreference.isNotificationEnabled().first()
            }

            setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    userPreference.setNotificationEnabled(isChecked)
                }
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@SettingActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            ReminderManager.setupDailyReminders(this@SettingActivity)
                            showImmediateNotification()
                            showNotificationToast(true)
                        } else {
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        ReminderManager.setupDailyReminders(this@SettingActivity)
                        showImmediateNotification()
                        showNotificationToast(true)
                    }
                } else {
                    ReminderManager.cancelDailyReminders(this@SettingActivity)
                    showNotificationToast(false)
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showImmediateNotification() {
        ReminderManager.triggerImmediateNotification(this)
    }

    private fun showNotificationToast(isEnabled: Boolean) {
        val message = if (isEnabled) {
            "Peningat diatur pada pukul 07, 13, dan 19"
        } else {
            "Pengingat dimatikan"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

