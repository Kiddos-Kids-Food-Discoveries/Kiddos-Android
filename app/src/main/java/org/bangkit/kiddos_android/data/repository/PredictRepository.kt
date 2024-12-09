package org.bangkit.kiddos_android.data.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.bangkit.kiddos_android.data.remote.api.ApiService
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import retrofit2.Response

class PredictRepository(private val apiService: ApiService) {

    suspend fun predict(file: MultipartBody.Part, userId: RequestBody): Response<PredictResponse> {
        return apiService.predict(file, userId)
    }
}
