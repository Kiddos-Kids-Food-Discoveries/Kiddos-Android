package org.bangkit.kiddos_android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.databinding.ActivityRegisterBinding
import org.bangkit.kiddos_android.ui.viewmodel.RegisterViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var viewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        })

        setupViewModel()
        setupAction()
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory.getInstance(this)
        )[RegisterViewModel::class.java]
    }

    private fun setupAction() {
        binding.btnContinueSignUp.setOnClickListener {
            val name = binding.etNama.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etRePassword.text.toString()

            when {
                name.isEmpty() -> {
                    binding.etNama.error = getString(R.string.name_required)
                    binding.etNama.requestFocus()
                }

                email.isEmpty() -> {
                    binding.etEmail.error = getString(R.string.email_required)
                    binding.etEmail.requestFocus()
                }

                password.isEmpty() -> {
                    binding.etPassword.error = getString(R.string.password_required)
                    binding.etPassword.requestFocus()
                }

                password.length < 8 -> {
                    binding.etPassword.error = getString(R.string.password_min_length)
                    binding.etPassword.requestFocus()
                }

                password != confirmPassword -> {
                    binding.etRePassword.error = getString(R.string.password_mismatch)
                    binding.etRePassword.requestFocus()
                }

                else -> {
                    showLoading(true)
                    viewModel.register(name, email, password)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.registrationResult.observe(this) { result ->
            result.fold(
                onSuccess = { response ->
                    showLoading(false)
                    if (!response.error) {
                        Toast.makeText(
                            this@RegisterActivity,
                            getString(R.string.register_successful),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onFailure = { exception ->
                    showLoading(false)
                    Toast.makeText(
                        this@RegisterActivity,
                        exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnContinueSignUp.isEnabled = !isLoading
        binding.etNama.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etRePassword.isEnabled = !isLoading
    }
}
