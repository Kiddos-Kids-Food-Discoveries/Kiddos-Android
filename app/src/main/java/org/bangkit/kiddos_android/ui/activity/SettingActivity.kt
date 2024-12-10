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
            ReminderManager.setupDailyReminder(this)
            showImmediateNotification()
        } else {
            Toast.makeText(this, "Dibutuhkan izin notifkasi untuk mengaktifkan pengingat harian.", Toast.LENGTH_SHORT).show()
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
                        if (ContextCompat.checkSelfPermission(this@SettingActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            ReminderManager.setupDailyReminder(this@SettingActivity)
                            showImmediateNotification()
                        } else {
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        ReminderManager.setupDailyReminder(this@SettingActivity)
                        showImmediateNotification()
                    }
                } else {
                    ReminderManager.cancelDailyReminder(this@SettingActivity)
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
}
