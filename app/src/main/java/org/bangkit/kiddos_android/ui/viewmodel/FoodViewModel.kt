package org.bangkit.kiddos_android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.repository.FoodRepository
import org.bangkit.kiddos_android.domain.model.FoodItem

class FoodViewModel(private val foodRepository: FoodRepository) : ViewModel() {

    private val _foodData = MutableLiveData<List<FoodItem>?>()
    val foodData: LiveData<List<FoodItem>?> get() = _foodData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getFoodByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = foodRepository.getFoodByCategory(category)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _foodData.value = response.body()?.data
                } else {
                    _foodData.value = null
                }
            } catch (e: Exception) {
                _foodData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
