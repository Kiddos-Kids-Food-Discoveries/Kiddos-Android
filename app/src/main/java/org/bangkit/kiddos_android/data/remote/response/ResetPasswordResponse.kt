package org.bangkit.kiddos_android.data.remote.response

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("resetlink")
    val resetlink: String? = null
)