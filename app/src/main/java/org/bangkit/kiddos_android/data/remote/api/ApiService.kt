package org.bangkit.kiddos_android.data.remote.api

import org.bangkit.kiddos_android.data.remote.response.LoginResponse
import org.bangkit.kiddos_android.data.remote.response.RegisterResponse
import org.bangkit.kiddos_android.data.remote.response.ResetPasswordResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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

    @FormUrlEncoded
    @POST("forgotPassword")
    suspend fun resetPassword(
        @Field("email") email: String
    ): ResetPasswordResponse
}