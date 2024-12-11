package org.bangkit.kiddos_android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.domain.model.User

class AccountDetailViewModel(
    private val userPreference: UserPreference,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchNameFromPreferences()
    }

    private fun fetchNameFromPreferences() {
        viewModelScope.launch {
            userPreference.getName().collect { name ->
                _name.value = name
            }
        }
    }

    fun fetchUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userResponse = userRepository.getUser(userId)

                Log.d("AccountDetailViewModel", "fetchUser response: $userResponse")

                val fetchedUser = userResponse[userId]
                _user.value = fetchedUser

                Log.d("AccountDetailViewModel", "Fetched user: $fetchedUser")
            } catch (e: Exception) {
                _user.value = null
                Log.e("AccountDetailViewModel", "Error fetching user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}


