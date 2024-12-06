package org.bangkit.kiddos_android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.ArticleRepository
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.databinding.FragmentProfileBinding
import org.bangkit.kiddos_android.ui.activity.AccountDetailActivity
import org.bangkit.kiddos_android.ui.activity.LoginActivity
import org.bangkit.kiddos_android.ui.viewmodel.HomeViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.HomeViewModelFactory

class ProfileFragment : Fragment() {

    private lateinit var userPreference: UserPreference
    private lateinit var binding: FragmentProfileBinding

    private val userRepository by lazy { UserRepository(ApiConfig.getApiService()) }
    private val articleRepository by lazy { ArticleRepository(ApiConfig.getApiService()) }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(userRepository, articleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userPreference = UserPreference.getInstance(requireContext())

        // Observe LiveData
        observeViewModel()

        // Fetch data if not already loaded
        if (homeViewModel.user.value == null) {
            fetchUserInfo()
        }

        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
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

    private fun observeViewModel() {
        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileName.text = it.name
                Glide.with(this)
                    .load(it.userPicture)
                    .into(binding.userImage)
            } ?: run {
                binding.profileName.text = getString(R.string.guest)
            }
        }
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            userPreference.getUserId().collect { userId ->
                if (userId.isNotEmpty()) {
                    homeViewModel.fetchUser(userId)
                } else {
                    binding.profileName.text = getString(R.string.guest)
                }
            }
        }
    }
}
