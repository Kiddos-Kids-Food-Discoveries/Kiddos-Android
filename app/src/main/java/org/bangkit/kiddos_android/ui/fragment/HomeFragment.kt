package org.bangkit.kiddos_android.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.ArticleRepository
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.databinding.FragmentHomeBinding
import org.bangkit.kiddos_android.ui.adapter.ArticlesAdapter
import org.bangkit.kiddos_android.ui.viewmodel.HomeViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.HomeViewModelFactory

class HomeFragment : Fragment() {

    private lateinit var articlesAdapter: ArticlesAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference
    private lateinit var helloNameTextView: MaterialTextView
    private lateinit var userImageView: ShapeableImageView

    private val userRepository by lazy { UserRepository(ApiConfig.getApiService()) }
    private val articleRepository by lazy { ArticleRepository(ApiConfig.getApiService()) }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(userRepository, articleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        userPreference = UserPreference.getInstance(requireContext())

        helloNameTextView = binding.HelloName
        userImageView = binding.userImage  // Initialize userImageView

        // Set up RecyclerView
        setupRecyclerView()

        // Observe LiveData
        observeViewModel()

        // Fetch data if not already loaded
        if (homeViewModel.user.value == null || homeViewModel.articles.value.isNullOrEmpty()) {
            fetchUserInfo()
            fetchArticles()
        }

        // Set click listener for scanCardView
        binding.cardViewMain.setOnClickListener {
            Log.d("HomeFragment", "Navigating to ScanFragment")
            findNavController().navigate(R.id.action_navigation_home_to_navigation_scan)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        articlesAdapter = ArticlesAdapter(emptyList()) { article ->
            Toast.makeText(requireContext(), "Clicked: ${article.title}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewArticles.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)  // Set spanCount to 2
            adapter = articlesAdapter
        }
    }

    private fun observeViewModel() {
        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                helloNameTextView.text = "Hello, ${it.name}"
                // Load user image using Glide
                Glide.with(this)
                    .load(it.userPicture)
                    .into(userImageView)
            } ?: run {
                helloNameTextView.text = "Hello, Guest"
            }
            binding.progressBarUser.visibility = View.GONE  // Hide progress bar
        }

        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articlesAdapter = ArticlesAdapter(articles) { article ->
                Toast.makeText(requireContext(), "Clicked: ${article.title}", Toast.LENGTH_SHORT).show()
            }
            binding.recyclerViewArticles.adapter = articlesAdapter
            binding.progressBarArticle.visibility = View.GONE  // Hide progress bar
        }
    }

    private fun fetchArticles() {
        binding.progressBarArticle.visibility = View.VISIBLE  // Show progress bar
        homeViewModel.fetchArticles()
    }

    private fun fetchUserInfo() {
        binding.progressBarUser.visibility = View.VISIBLE  // Show progress bar
        lifecycleScope.launch {
            userPreference.getUserId().collect { userId ->
                if (userId.isNotEmpty()) {
                    homeViewModel.fetchUser(userId)
                } else {
                    helloNameTextView.text = "Hello, Guest"
                    binding.progressBarUser.visibility = View.GONE  // Hide progress bar
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clean up binding reference to avoid memory leaks
    }
}
