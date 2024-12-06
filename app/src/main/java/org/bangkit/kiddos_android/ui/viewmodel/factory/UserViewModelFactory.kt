package org.bangkit.kiddos_android.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.ui.viewmodel.HomeViewModel
import org.bangkit.kiddos_android.ui.viewmodel.UserViewModel

class UserViewModelFactory(
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {  // Correct method signature
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
