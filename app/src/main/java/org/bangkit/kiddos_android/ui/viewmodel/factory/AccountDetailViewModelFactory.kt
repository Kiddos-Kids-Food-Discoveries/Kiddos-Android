package org.bangkit.kiddos_android.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.ui.viewmodel.AccountDetailViewModel

class ViewModelFactory(
    private val userPreference: UserPreference,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountDetailViewModel(userPreference, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


