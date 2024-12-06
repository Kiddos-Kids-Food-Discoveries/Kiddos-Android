package org.bangkit.kiddos_android.data.repository

import org.bangkit.kiddos_android.data.remote.api.ApiService
import org.bangkit.kiddos_android.domain.model.User

class UserRepository(private val apiService: ApiService) {
    suspend fun getUser(userId: String): Map<String, User> {
        return apiService.getUser(userId)
    }
}


