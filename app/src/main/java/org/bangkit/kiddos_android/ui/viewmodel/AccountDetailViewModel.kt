package org.bangkit.kiddos_android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.preferences.UserPreference

class AccountDetailViewModel(private val userPreference: UserPreference) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchUserData() {

        viewModelScope.launch {
            try {
                userPreference.getName().collect { userName ->
                    Log.d("ssss", "Fetched Name: $userName")
                    _name.value = userName
                }

                userPreference.getUserId().collect { id ->
                    _userId.value = id
                }

            } catch (e: Exception) {
                Log.e("AccountDetailViewModel", "Error fetching user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


}
