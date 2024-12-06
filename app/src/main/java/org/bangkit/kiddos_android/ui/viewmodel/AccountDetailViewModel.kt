package org.bangkit.kiddos_android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig

class AccountDetailViewModel(private val userPreference: UserPreference) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _userId = MutableLiveData<String>()  // Add MutableLiveData for userId
    val userId: LiveData<String> get() = _userId

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Fetch user data from DataStore
    fun fetchUserData() {

        viewModelScope.launch {
            try {
                userPreference.getName().collect { userName ->
                    Log.d("ssss", "Fetched Name: $userName")
                    _name.value = userName // Correctly setting the value of _name
                }

                userPreference.getUserId().collect { id ->
                    _userId.value = id  // Correctly setting the value of _userId
                }

            } catch (e: Exception) {
                Log.e("AccountDetailViewModel", "Error fetching user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


}
