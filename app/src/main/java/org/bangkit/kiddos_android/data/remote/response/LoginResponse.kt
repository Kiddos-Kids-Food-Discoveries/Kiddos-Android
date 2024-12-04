package org.bangkit.kiddos_android.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("user_id")
    val userId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("firebase_uid")
    val token: String,

    @field:SerializedName("email")
    val email: String
)