package org.bangkit.kiddos_android.data.repository

import android.util.Log

import org.bangkit.kiddos_android.data.remote.api.ApiService
import org.bangkit.kiddos_android.data.remote.response.ArticleResponse
import org.bangkit.kiddos_android.domain.model.Data
import retrofit2.Response

class ArticleRepository(private val apiService: ApiService) {
    suspend fun getArticles(): List<Data> {
        val response: Response<ArticleResponse> = apiService.getArticles()  // Panggilan API
        if (response.isSuccessful) {
            val articleResponse = response.body()
            return articleResponse?.data ?: emptyList()  // Mengambil data artikel atau mengembalikan list kosong jika null
        } else {
            Log.e("API", "Error: ${response.message()}")
            return emptyList()  // Mengembalikan list kosong jika response gagal
        }
    }
}
