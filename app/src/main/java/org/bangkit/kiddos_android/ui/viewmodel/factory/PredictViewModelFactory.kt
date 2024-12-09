package org.bangkit.kiddos_android.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.data.repository.PredictRepository
import org.bangkit.kiddos_android.ui.viewmodel.PredictViewModel

class PredictViewModelFactory(private val repository: PredictRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictViewModel::class.java)) {
            return PredictViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
