package org.bangkit.kiddos_android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.ArticleRepository
import org.bangkit.kiddos_android.data.repository.FoodRepository
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.databinding.FragmentHomeBinding
import org.bangkit.kiddos_android.ui.activity.FoodListActivity
import org.bangkit.kiddos_android.ui.adapter.ArticlesAdapter
import org.bangkit.kiddos_android.ui.viewmodel.FoodViewModel
import org.bangkit.kiddos_android.ui.viewmodel.HomeViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.FoodViewModelFactory
import org.bangkit.kiddos_android.ui.viewmodel.factory.HomeViewModelFactory
import org.bangkit.kiddos_android.utils.NetworkUtils

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

    private lateinit var foodViewModel: FoodViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        userPreference = UserPreference.getInstance(requireContext())

        helloNameTextView = binding.HelloName
        userImageView = binding.userImage

        setupRecyclerView()

        observeViewModel()

        loadUserName()

        if (homeViewModel.user.value == null || homeViewModel.articles.value.isNullOrEmpty()) {
            fetchUserInfo()
            fetchArticles()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchArticles()
            fetchUserInfo()
        }

        binding.cardViewMain.setOnClickListener {
            Log.d("HomeFragment", "Navigating to ScanFragment")
            findNavController().navigate(R.id.action_navigation_home_to_navigation_scan)
        }

        binding.profileImage.setOnClickListener {
            Log.d("HomeFragment", "Navigating to ProfileFragment")
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiConfig.getApiService()
        val foodRepository = FoodRepository(apiService)
        val viewModelFactory = FoodViewModelFactory(foodRepository)
        foodViewModel = ViewModelProvider(this, viewModelFactory).get(FoodViewModel::class.java)

        binding.cardFoodVegetable.setOnClickListener {
            navigateToFoodListActivity("sayur")
        }

        binding.cardFoodFruit.setOnClickListener {
            navigateToFoodListActivity("buah")
        }

        binding.cardFoodFood.setOnClickListener {
            navigateToFoodListActivity("makanan")
        }
    }

    private fun navigateToFoodListActivity(category: String) {
        val intent = Intent(requireContext(), FoodListActivity::class.java).apply {
            putExtra("CATEGORY", category)
        }
        startActivity(intent)
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
                lifecycleScope.launch {
                    userPreference.saveUserPicture(it.userPicture)
                }

                Log.d("HomeFragment", "User Picture URL: ${it.userPicture}")
                Glide.with(this)
                    .load(it.userPicture + "?timestamp=${System.currentTimeMillis()}")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(binding.userImage)
            }
        }

        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articlesAdapter = ArticlesAdapter(articles) { article ->
                Toast.makeText(requireContext(), "Clicked: ${article.title}", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.recyclerViewArticles.adapter = articlesAdapter
            binding.progressBarArticle.visibility = View.GONE
            // Stop the refresh animation once the data is fetched
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadUserName() {
        lifecycleScope.launch {
            userPreference.getName().collect { userName ->
                helloNameTextView.text = if (userName.isNotEmpty()) {
                    "Hello, $userName"
                } else {
                    "Hello, Guest"
                }
            }
        }
    }

    private fun fetchArticles() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            binding.progressBarArticle.visibility = View.VISIBLE
            homeViewModel.fetchArticles()
        } else {
            Toast.makeText(requireContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            userPreference.getUserId().collect { userId ->
                if (_binding != null) {
                    if (userId.isNotEmpty()) {
                        homeViewModel.fetchUser(userId)
                    } else {
                        helloNameTextView.text = getString(R.string.hello_guest)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

