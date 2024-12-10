package org.bangkit.kiddos_android.data.remote.response

import com.google.gson.annotations.SerializedName
import org.bangkit.kiddos_android.domain.model.FoodItem

data class FoodResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<FoodItem>
)

