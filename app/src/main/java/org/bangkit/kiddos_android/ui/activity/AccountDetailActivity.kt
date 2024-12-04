package org.bangkit.kiddos_android.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.remote.response.ResetPasswordResponse
import org.bangkit.kiddos_android.databinding.ActivityAccountDetailBinding
import org.bangkit.kiddos_android.ui.viewmodel.AccountDetailViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AccountDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var accountDetailViewModel: AccountDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.changePassButton.setOnClickListener {
            val email = binding.editEmail.text.toString()

            if (email.isNotEmpty()) {
                // Show confirmation dialog before resetting password
                showConfirmationDialog(email)
            } else {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize ViewModel with UserPreference
        accountDetailViewModel = ViewModelProvider(this, ViewModelFactory(UserPreference.getInstance(applicationContext)))
            .get(AccountDetailViewModel::class.java)

        accountDetailViewModel.name.observe(this, Observer { name ->
            binding.tvName.text = name
            binding.editName.setText(name)
        })

        lifecycleScope.launch {
            UserPreference.getInstance(applicationContext).getEmail().collect { userEmail ->
                binding.editEmail.setText(userEmail)
            }
        }

        // Observe loading state
        accountDetailViewModel.isLoading.observe(this, Observer { isLoading ->
            // Show or hide a loading indicator if necessary
            if (isLoading) {
                // Show loading (e.g., show progress bar)
            } else {
                // Hide loading
            }
        })

        // Fetch user data
        accountDetailViewModel.fetchUserData()

        // Back button click listener
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showConfirmationDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.reset_password_confirmation_title))
        builder.setMessage(getString(R.string.reset_password_confirmation_message, email))
        builder.setPositiveButton(getString(R.string.reset_password_button_yes)) { dialog, _ ->
            // Proceed with password reset
            requestPasswordReset(email)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.reset_password_button_no)) { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog
        }
        builder.show()
    }

    // Function to make the API request for password reset
    private fun requestPasswordReset(email: String) {
        // Show progress bar
        binding.progressBarLoading.visibility = android.view.View.VISIBLE

        // Launch the request inside a coroutine
        lifecycleScope.launch {
            try {
                // Make the API call inside the IO dispatcher for networking
                val response = withContext(Dispatchers.IO) {
                    // Call the suspend function to request password reset
                    ApiConfig.getApiService().resetPassword(email)
                }

                // Hide progress bar after the request
                binding.progressBarLoading.visibility = android.view.View.GONE

                // Handle the response
                if (response.error == false) {
                    val resetLink = response.resetlink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resetLink))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@AccountDetailActivity, getString(R.string.failed_to_send_reset_link), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Hide progress bar on failure
                binding.progressBarLoading.visibility = android.view.View.GONE
                Toast.makeText(this@AccountDetailActivity, getString(R.string.request_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
