package org.bangkit.kiddos_android.data.repository

import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.remote.response.HistoryResponse
import retrofit2.Response

class HistoryRepository {

    private val apiService = ApiConfig.getApiService()

    suspend fun getHistory(userId: String): Response<HistoryResponse> {
        return apiService.getHistory(userId)
    }

    suspend fun deleteHistory(historyId: String): Response<Any> {
        return apiService.deleteHistory(historyId)
    }
}
