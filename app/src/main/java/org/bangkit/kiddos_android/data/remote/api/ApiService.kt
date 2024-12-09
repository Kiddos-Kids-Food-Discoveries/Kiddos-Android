package org.bangkit.kiddos_android.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.bangkit.kiddos_android.data.remote.response.ArticleResponse
import org.bangkit.kiddos_android.data.remote.response.HistoryResponse
import org.bangkit.kiddos_android.data.remote.response.LoginResponse
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import org.bangkit.kiddos_android.data.remote.response.RegisterResponse
import org.bangkit.kiddos_android.data.remote.response.ResetPasswordResponse
import org.bangkit.kiddos_android.data.remote.response.UpdateUserResponse
import org.bangkit.kiddos_android.domain.model.User
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse


    @GET("users/{iduser}")
    suspend fun getUser(
        @Path("iduser") userId: String
    ): Map<String, User>

    @Multipart
    @POST("predicthisto")
    suspend fun predict(
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Response<PredictResponse>


    @GET("articles")
    suspend fun getArticles(
    ): Response<ArticleResponse>

    @GET("history/{user_id}")
    suspend fun getHistory(
        @Path("user_id") userId: String
    ): Response<HistoryResponse>

    @DELETE("history/{history_id}")
    suspend fun deleteHistory(
        @Path("history_id") historyId: String
    ): Response<Any>

    @FormUrlEncoded
    @POST("forgotPassword")
    suspend fun resetPassword(
        @Field("email") email: String
    ): ResetPasswordResponse

    @Multipart
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Part userPicture: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("userId") userIdField: RequestBody
    ): Response<UpdateUserResponse>
}

