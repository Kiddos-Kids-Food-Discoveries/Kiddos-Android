package org.bangkit.kiddos_android.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    @SerializedName("id") val id: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("nutrition") val nutrition: NutritionFood,
    @SerializedName("makanan_picture") val makananPicture: String?
) : Parcelable

@Parcelize
data class NutritionFood(
    @SerializedName("kalori") val kalori: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("karbohidrat") val karbohidrat: Double,
    @SerializedName("vitamin A") val vitaminA: String,
    @SerializedName("vitamin C") val vitaminC: String
) : Parcelable
