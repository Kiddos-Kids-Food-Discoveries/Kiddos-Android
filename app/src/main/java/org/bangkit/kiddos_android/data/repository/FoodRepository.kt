package org.bangkit.kiddos_android.data.repository

import org.bangkit.kiddos_android.data.remote.api.ApiService
import org.bangkit.kiddos_android.data.remote.response.FoodResponse
import retrofit2.Response

class FoodRepository(private val apiService: ApiService) {

    suspend fun getFoodByCategory(category: String): Response<FoodResponse> {
        return apiService.getFoodByCategory(category)
    }
}
