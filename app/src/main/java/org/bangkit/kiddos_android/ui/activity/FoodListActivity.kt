package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.FoodRepository
import org.bangkit.kiddos_android.databinding.ActivityFoodListBinding
import org.bangkit.kiddos_android.ui.adapter.FoodAdapter
import org.bangkit.kiddos_android.ui.viewmodel.FoodViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.FoodViewModelFactory
import java.util.Locale

class FoodListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodListBinding
    private lateinit var foodViewModel: FoodViewModel
    private lateinit var foodAdapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra("CATEGORY") ?: "makanan"
        binding.tvTopBarTitle.text = capitalizeWords(category)

        val apiService = ApiConfig.getApiService()
        val foodRepository = FoodRepository(apiService)
        val viewModelFactory = FoodViewModelFactory(foodRepository)
        foodViewModel = ViewModelProvider(this, viewModelFactory).get(FoodViewModel::class.java)

        foodViewModel.foodData.observe(this) { foodData ->
            foodData?.let {
                foodAdapter = FoodAdapter(it)
                binding.rvFood.apply {
                    layoutManager = GridLayoutManager(this@FoodListActivity, 2)
                    adapter = foodAdapter
                }
            } ?: run {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        }

        foodViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        foodViewModel.getFoodByCategory(category)
    }

    private fun capitalizeWords(text: String?): String {
        return text?.split(" ")?.joinToString(" ") { it.capitalize(Locale.ROOT) } ?: ""
    }
}
