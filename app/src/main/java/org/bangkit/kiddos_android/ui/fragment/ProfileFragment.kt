package org.bangkit.kiddos_android.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.ArticleRepository
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.databinding.FragmentProfileBinding
import org.bangkit.kiddos_android.ui.activity.AccountDetailActivity
import org.bangkit.kiddos_android.ui.activity.LoginActivity
import org.bangkit.kiddos_android.ui.activity.SettingActivity
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
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userPreference = UserPreference.getInstance(requireContext())

        val swipeRefreshLayout = binding.root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserInfo()
        }

        observeViewModel()
        loadUserName()

        if (homeViewModel.user.value == null) {
            fetchUserInfo()
        }

        setupListeners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.navigation_home)
            }
        })

        return binding.root
    }


    private fun setupListeners() {
        binding.actionLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.buttonSetting.setOnClickListener {
            navigateToSettingActivity()
        }
        binding.buttonDetail.setOnClickListener {
            navigateToAccountDetailActivity()
        }
        binding.buttonEmail.setOnClickListener {
            sendEmail()
        }

    }

    private fun navigateToSettingActivity() {
        val intent = Intent(requireContext(), SettingActivity::class.java)
        startActivity(intent)
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
                    userPreference.clearToken()

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

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("c014b4ky1807@bangkit.academy"))
            putExtra(Intent.EXTRA_SUBJECT, "Kiddos")
        }
        if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }



    private fun observeViewModel() {
        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                Log.d("HomeFragment", "User Picture URL: ${it.userPicture}")

                Glide.with(this)
                    .load(it.userPicture + "?timestamp=${System.currentTimeMillis()}") // Tambahkan timestamp untuk invalidasi cache
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(binding.userImage)


            } ?: run {
                Log.d("HomeFragment", "User is null or guest user")
                binding.profileName.text = getString(R.string.guest)
            }
        }
    }


    private fun fetchUserInfo() {
        lifecycleScope.launch {
            userPreference.getUserId().collect { userId ->
                if (userId.isNotEmpty()) {
                    homeViewModel.fetchUser(userId)
                    binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation when done
                } else {
                    binding.profileName.text = getString(R.string.guest)
                    binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
                }
            }
        }
    }


    private fun loadUserName() {
        lifecycleScope.launch {
            userPreference.getName().collect { userName ->
                binding.profileName.text = if (userName.isNotEmpty()) {
                    userName
                } else {
                    "Guest"
                }
            }
        }
    }
}
