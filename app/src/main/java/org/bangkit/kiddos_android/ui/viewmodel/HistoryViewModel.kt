package org.bangkit.kiddos_android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.repository.HistoryRepository
import org.bangkit.kiddos_android.domain.model.HistoryItem

class HistoryViewModel(private val repository: HistoryRepository, private val userPreference: UserPreference) : ViewModel() {

    private val _history = MutableLiveData<List<HistoryItem>>()
    val history: LiveData<List<HistoryItem>> get() = _history

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = userPreference.getUserId().first()
            val response = repository.getHistory(userId)
            if (response.isSuccessful) {
                _history.value = response.body()?.data
            } else {
                _history.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun deleteHistory(historyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.deleteHistory(historyId)
            if (response.isSuccessful) {
                // Update the history list after successful deletion
                _history.value = _history.value?.filterNot { it.id == historyId }
            }
            _isLoading.value = false
        }
    }
}
