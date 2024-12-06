package org.bangkit.kiddos_android.data.remote.response

import com.google.gson.annotations.SerializedName

data class UpdateUserResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null
)