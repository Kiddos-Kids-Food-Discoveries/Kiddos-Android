package org.bangkit.kiddos_android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import org.bangkit.kiddos_android.data.repository.PredictRepository

class PredictViewModel(private val repository: PredictRepository) : ViewModel() {

    private val _predictResult = MutableLiveData<PredictResponse?>() // Ubah menjadi nullable
    val predictResult: LiveData<PredictResponse?> get() = _predictResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun predict(file: MultipartBody.Part, userId: RequestBody) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.predict(file, userId)
                if (response.isSuccessful) {
                    _predictResult.value = response.body()
                } else {
                    // Handle API error
                }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPredictResult() {
        _predictResult.value = null
    }
}

