package org.bangkit.kiddos_android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.bangkit.kiddos_android.R
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.databinding.FragmentProfileBinding // Import ViewBinding class
import org.bangkit.kiddos_android.ui.activity.AccountDetailActivity
import org.bangkit.kiddos_android.ui.activity.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var userPreference: UserPreference
    private lateinit var binding: FragmentProfileBinding // Declare ViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding for the fragment's layout
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userPreference = UserPreference.getInstance(requireContext()) // Initialize UserPreference

        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        // Set up the logout button listener
        binding.actionLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.buttonDetail.setOnClickListener {
            navigateToAccountDetailActivity()
        }
    }

    private fun navigateToAccountDetailActivity() {
        val intent = Intent(requireContext(), AccountDetailActivity::class.java)
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.logout_confirmation_yes)) { dialog, _ ->
                lifecycleScope.launch {
                    userPreference.clearToken() // Clear user token to log out

                    showToast(getString(R.string.logout_successful))

                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.logout_confirmation_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
