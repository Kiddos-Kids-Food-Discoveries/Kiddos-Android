package org.bangkit.kiddos_android.di

import android.content.Context
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.AuthRepository

object Injection {
    fun provideRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        val preferences = UserPreference.getInstance(context)
        return AuthRepository.getInstance(apiService, preferences)
    }

}