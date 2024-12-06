package org.bangkit.kiddos_android.data.repository

import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiService
import org.bangkit.kiddos_android.data.remote.response.LoginResponse
import org.bangkit.kiddos_android.data.remote.response.RegisterResponse

class AuthRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun register(name: String, email: String, password: String): RegisterResponse =
        apiService.register(name, email, password)

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(email, password)
        userPreference.saveToken(response.token)
        val userId = response.userId
        userPreference.saveUserInfo(response.email, userId)
        userPreference.saveName(response.name)

        return response
    }

    fun getToken() = userPreference.getToken()

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(apiService, userPreference).also { instance = it }
            }
        }
    }
}
