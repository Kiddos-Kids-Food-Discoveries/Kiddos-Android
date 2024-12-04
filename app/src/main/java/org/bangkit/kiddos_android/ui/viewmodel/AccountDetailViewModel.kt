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

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Fetch user data from DataStore
    fun fetchUserData() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                userPreference.getName().collect { userName ->
                    Log.d("ssss", "Fetched Name: $userName")
                    _name.value = userName
                }

                userPreference.getEmail().collect { userEmail ->
                    Log.d("ssss", "Fetched Email: $userEmail")
                    _email.value = userEmail
                }

            } catch (e: Exception) {
                Log.e("AccountDetailViewModel", "Error fetching user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Optionally: Add methods to update user data
    fun updateUserInfo(newName: String, newEmail: String) {
        viewModelScope.launch {
            try {
                userPreference.saveUserInfo(newName, newEmail)
                _name.value = newName
                _email.value = newEmail
            } catch (e: Exception) {
                Log.e("AccountDetailViewModel", "Error saving user info", e)
            }
        }
    }
}
