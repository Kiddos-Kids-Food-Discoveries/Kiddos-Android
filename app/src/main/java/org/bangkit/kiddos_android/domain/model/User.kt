package org.bangkit.kiddos_android.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("firebase_uid")
    val firebaseUid: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("user_picture")
    val userPicture: String,

    @SerializedName("name")
    val name: String
)