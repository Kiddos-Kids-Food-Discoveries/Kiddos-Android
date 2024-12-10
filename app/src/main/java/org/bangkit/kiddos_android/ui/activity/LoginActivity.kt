package org.bangkit.kiddos_android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.databinding.ActivityLoginBinding
import org.bangkit.kiddos_android.ui.viewmodel.LoginViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.AuthViewModelFactory


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        setupAction()
        observeViewModel()
        checkSession()
    }


    private fun checkSession() {
        viewModel.token.observe(this) { event ->
            event.getContentIfNotHandled()?.let { token ->
                if (token.isNotEmpty()) navigateToHome()
            }
        }
    }

    private fun setupAction() {
        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            when {
                email.isEmpty() -> binding.etEmail.apply {
                    error = getString(R.string.email_required)
                    requestFocus()
                }

                password.isEmpty() -> binding.etPassword.apply {
                    error = getString(R.string.password_required)
                    requestFocus()
                }

                password.length < 8 -> binding.etPassword.apply {
                    error = getString(R.string.password_min_length)
                    requestFocus()
                }

                else -> {
                    showLoading(true)
                    viewModel.login(email, password)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResult.observe(this) { result ->
            result.fold(
                onSuccess = { response ->
                    showLoading(false)
                    if (!response.error) {
                        Toast.makeText(
                            this,
                            getString(R.string.login_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { exception ->
                    showLoading(false)
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnContinue.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}
