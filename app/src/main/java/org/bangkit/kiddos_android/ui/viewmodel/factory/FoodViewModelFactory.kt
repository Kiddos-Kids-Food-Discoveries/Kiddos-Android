package org.bangkit.kiddos_android.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.data.repository.FoodRepository
import org.bangkit.kiddos_android.ui.viewmodel.FoodViewModel

class FoodViewModelFactory(private val foodRepository: FoodRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            return FoodViewModel(foodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
